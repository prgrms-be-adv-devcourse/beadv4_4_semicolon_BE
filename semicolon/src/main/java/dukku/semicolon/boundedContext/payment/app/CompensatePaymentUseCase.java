package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.boundedContext.payment.out.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 결제 실패(보상 트랜잭션용) UseCase
 *
 * <p>
 * 예치금 차감 실패 등 후속 프로세스 오류 시 이미 승인된 PG 결제를 취소 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensatePaymentUseCase {

    private final PaymentSupport support;
    private final TossPaymentClient tossClient;

    /**
     * 보상 트랜잭션 실행 (PG 결제 취소)
     *
     * @param orderUuid 관련 주문 UUID
     * @param reason    취소 사유
     */
    @Transactional
    public void execute(UUID orderUuid, String reason) {
        // 1. 주문 UUID로 결제 정보 조회 (최근 승인된 것 우선)
        Payment payment = support.findPaymentsByOrderUuid(orderUuid).stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.DONE)
                .findFirst()
                .orElse(null);

        if (payment == null) {
            log.warn("[보상 트랜잭션 건너뜀] 취소할 완료된 결제가 없음: orderUuid={}", orderUuid);
            return;
        }

        // 2. 상태 변경 전 값 저장 (이력용)
        PaymentStatus originStatus = payment.getPaymentStatus();
        Long originAmountPg = payment.getAmountPg();
        Long originDeposit = payment.getPaymentDeposit();

        // 3. 토스페이먼츠 취소 API 호출
        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancelReason", "시스템 오류로 인한 보상 트랜잭션: " + reason);

        Map<String, Object> response = tossClient.cancel(payment.getPgPaymentKey(), cancelBody);
        int statusCode = ((Number) response.getOrDefault("statusCode", 200)).intValue();

        if (statusCode >= 400) {
            log.error("[CRITICAL][보상 트랜잭션 오류] PG 취소 실패: status={}, body={}. 관리자 확인 필요! paymentUuid={}",
                    statusCode, response, payment.getUuid());

            // 상태를 ROLLBACK_FAILED로 변경하여 관리자 개입 유도
            payment.rollbackFailedStatus();
            support.savePayment(payment);

            support.createHistory(payment, PaymentHistoryType.PAYMENT_ROLLBACK_FAILED, originStatus, originAmountPg,
                    originDeposit);
            return;
        }

        // 4. 시스템 내 결제 취소 처리
        payment.cancel();
        support.savePayment(payment);

        // 5. 이력 기록
        support.createHistory(payment, PaymentHistoryType.PAYMENT_FAILED, originStatus, originAmountPg, originDeposit);

        log.info("[보상 트랜잭션 완료] 결제 취소됨: paymentUuid={}, orderUuid={}", payment.getUuid(), orderUuid);
    }
}
