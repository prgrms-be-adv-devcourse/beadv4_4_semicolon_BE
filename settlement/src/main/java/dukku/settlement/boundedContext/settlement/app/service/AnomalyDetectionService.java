package dukku.settlement.boundedContext.settlement.app.service;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.payment.dto.PaymentInternalResponse;
import dukku.common.shared.payment.out.PaymentApiClient;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.common.shared.settlement.event.SettlementAnomalyDetectedEvent;
import dukku.common.shared.settlement.type.AnomalyType;
import dukku.common.shared.settlement.type.SettlementStatus;
import dukku.common.shared.user.dto.UserProfileResponse;
import dukku.common.shared.user.out.UserApiClient;
import dukku.settlement.boundedContext.settlement.app.SettlementMetrics;
import dukku.settlement.boundedContext.settlement.batch.notification.AnomalyTracker;
import dukku.settlement.boundedContext.settlement.entity.Settlement;
import dukku.settlement.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이상거래 탐지 서비스
 * - 정산 건에 대해 6가지 규칙을 검사
 * - CRITICAL 탐지 시 settlement.fail(reason) 호출
 * - 모든 탐지 건에 대해 Kafka 이벤트 + Micrometer 메트릭 + AnomalyTracker 기록
 *
 * @return true if CRITICAL anomaly detected (settlement is now FAILED)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final PaymentApiClient paymentApiClient;
    private final UserApiClient userApiClient;
    private final SettlementRepository settlementRepository;
    private final EventPublisher eventPublisher;
    private final SettlementMetrics settlementMetrics;
    private final AnomalyTracker anomalyTracker;

    private static final long AMOUNT_TOLERANCE = 1L;
    private static final double REFUND_RATE_THRESHOLD = 0.15;
    private static final int NEW_SELLER_DAYS = 30;
    private static final long HIGH_RISK_AMOUNT = 100_000_000L;

    /**
     * 이상거래 탐지 실행
     * @return CRITICAL 이상거래가 탐지되어 FAILED 처리되었으면 true
     */
    public boolean detect(Settlement settlement) {
        PaymentInternalResponse payment = getPaymentSafely(settlement.getOrderId());
        List<String> criticalReasons = new ArrayList<>();

        // CRITICAL 규칙
        checkAmountMismatch(settlement, payment, criticalReasons);
        checkRefundedOrder(settlement, payment, criticalReasons);
        checkDuplicateSettlement(settlement, criticalReasons);

        // HIGH 규칙
        checkFeeCalculation(settlement);
        checkHighRefundRate(settlement);
        checkNewSellerHighRisk(settlement);

        if (!criticalReasons.isEmpty()) {
            String reason = String.join(" | ", criticalReasons);
            settlement.fail(reason);
            log.info("[이상거래 탐지] CRITICAL - settlementUuid: {}, reason: {}",
                    settlement.getUuid(), reason);
            return true;
        }

        return false;
    }

    // ── CRITICAL 규칙 ──

    // PG 결제금액과 플랫폼 정산 총액이 허용 오차(1원)를 초과하여 불일치하는지 검사
    private void checkAmountMismatch(Settlement settlement, PaymentInternalResponse payment,
                                     List<String> criticalReasons) {
        if (payment == null) return;
        long diff = Math.abs(settlement.getTotalAmount() - payment.getTotalAmount());
        if (diff > AMOUNT_TOLERANCE) {
            String desc = String.format("PG 결제금액(%d)과 정산 총액(%d) 불일치. 차이: %d원",
                    payment.getTotalAmount(), settlement.getTotalAmount(), diff);
            criticalReasons.add(desc);
            publishAndCount(settlement, AnomalyType.AMOUNT_MISMATCH, desc,
                    payment.getTotalAmount(), settlement.getTotalAmount());
        }
    }

    // 결제가 취소(CANCELED/PARTIAL_CANCELED)된 주문에 대해 정산이 진행되는지 검사
    private void checkRefundedOrder(Settlement settlement, PaymentInternalResponse payment,
                                    List<String> criticalReasons) {
        if (payment == null) return;
        if (payment.getStatus() == PaymentStatus.CANCELED
                || payment.getStatus() == PaymentStatus.PARTIAL_CANCELED) {
            String desc = String.format("환불된 주문(%s) 정산 시도. 결제상태: %s",
                    settlement.getOrderId(), payment.getStatus());
            criticalReasons.add(desc);
            publishAndCount(settlement, AnomalyType.REFUNDED_ORDER_SETTLEMENT, desc,
                    null, settlement.getTotalAmount());
        }
    }

    // 같은 주문(orderId)에 대해 FAILED가 아닌 정산이 2건 이상 존재하는지 검사
    private void checkDuplicateSettlement(Settlement settlement, List<String> criticalReasons) {
        long count = settlementRepository.countByOrderIdAndSettlementStatusNot(
                settlement.getOrderId(), SettlementStatus.FAILED);
        if (count >= 2) {
            String desc = String.format("동일 주문(%s) 중복 정산. FAILED 제외 건수: %d",
                    settlement.getOrderId(), count);
            criticalReasons.add(desc);
            publishAndCount(settlement, AnomalyType.DUPLICATE_SETTLEMENT, desc, 1L, count);
        }
    }

    // ── HIGH 규칙 ──

    // totalAmount와 (settlementAmount + feeAmount) 합계가 일치하는지 검사
    private void checkFeeCalculation(Settlement settlement) {
        long expected = settlement.getSettlementAmount() + settlement.getFeeAmount();
        if (!settlement.getTotalAmount().equals(expected)) {
            String desc = String.format("수수료 계산 오류. totalAmount(%d) != settlementAmount(%d) + feeAmount(%d)",
                    settlement.getTotalAmount(), settlement.getSettlementAmount(), settlement.getFeeAmount());
            publishAndCount(settlement, AnomalyType.FEE_CALCULATION_ERROR, desc, expected,
                    settlement.getTotalAmount());
        }
    }

    // 셀러의 최근 30일 정산 건 중 환불 비율이 15%를 초과하는지 검사
    private void checkHighRefundRate(Settlement settlement) {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Settlement> recent = settlementRepository.findBySellerUuidAndCreatedAtAfter(
                    settlement.getSellerUuid(), thirtyDaysAgo);
            if (recent.isEmpty()) return;

            long refundedCount = 0;
            for (Settlement s : recent) {
                PaymentInternalResponse p = getPaymentSafely(s.getOrderId());
                if (p != null && (p.getStatus() == PaymentStatus.CANCELED
                        || p.getStatus() == PaymentStatus.PARTIAL_CANCELED)) {
                    refundedCount++;
                }
            }

            double rate = (double) refundedCount / recent.size();
            if (rate > REFUND_RATE_THRESHOLD) {
                long ratePercent = Math.round(rate * 100);
                String desc = String.format("셀러(%s) 최근 30일 환불률 %d%% (기준: 15%%)",
                        settlement.getSellerUuid(), ratePercent);
                publishAndCount(settlement, AnomalyType.HIGH_REFUND_RATE_SELLER, desc,
                        15L, ratePercent);
            }
        } catch (Exception e) {
            log.warn("[이상거래 탐지] 환불률 체크 실패, 스킵 - sellerUuid: {}, error: {}",
                    settlement.getSellerUuid(), e.getMessage());
        }
    }

    // 가입 30일 이내 셀러의 정산 총액이 1억원을 초과하는지 검사
    private void checkNewSellerHighRisk(Settlement settlement) {
        try {
            UserProfileResponse profile = userApiClient.getUserProfile(settlement.getSellerUuid());
            if (profile == null) return;

            boolean isNewSeller = profile.getCreatedAt().isAfter(LocalDateTime.now().minusDays(NEW_SELLER_DAYS));
            boolean isHighAmount = settlement.getTotalAmount() > HIGH_RISK_AMOUNT;

            if (isNewSeller && isHighAmount) {
                String desc = String.format("신규 셀러(%s, 가입: %s) 고액 정산(%d원)",
                        settlement.getSellerUuid(), profile.getCreatedAt(), settlement.getTotalAmount());
                publishAndCount(settlement, AnomalyType.NEW_SELLER_HIGH_RISK, desc,
                        HIGH_RISK_AMOUNT, settlement.getTotalAmount());
            }
        } catch (Exception e) {
            log.warn("[이상거래 탐지] 신규 셀러 체크 실패, 스킵 - sellerUuid: {}, error: {}",
                    settlement.getSellerUuid(), e.getMessage());
        }
    }

    // ── 공통 ──

    private void publishAndCount(Settlement settlement, AnomalyType type, String description,
                                 Long expectedValue, Long actualValue) {
        settlementMetrics.incrementAnomalyDetected(type.name(), type.getSeverity().name(), settlement.getUuid().toString());
        anomalyTracker.record(type, settlement.getUuid(), settlement.getOrderId(), description);

        eventPublisher.publish(new SettlementAnomalyDetectedEvent(
                settlement.getUuid(),
                settlement.getSellerUuid(),
                settlement.getOrderId(),
                type.name(),
                type.getSeverity().name(),
                description,
                expectedValue,
                actualValue,
                LocalDateTime.now()
        ));
    }

    private PaymentInternalResponse getPaymentSafely(UUID orderUuid) {
        try {
            return paymentApiClient.getPaymentByOrderUuid(orderUuid);
        } catch (Exception e) {
            log.warn("[이상거래 탐지] 결제 정보 조회 실패, 관련 규칙 스킵 - orderUuid: {}, error: {}",
                    orderUuid, e.getMessage());
            return null;
        }
    }
}
