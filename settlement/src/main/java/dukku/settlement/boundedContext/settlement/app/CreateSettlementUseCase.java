package dukku.settlement.boundedContext.settlement.app;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.settlement.boundedContext.settlement.entity.Settlement;
import dukku.settlement.boundedContext.settlement.entity.SettlementSchedulePolicy;
import dukku.common.shared.deposit.out.depositApiClient.DepositApiClient;
import dukku.common.shared.order.out.OrderApiClient;
import dukku.common.shared.payment.dto.PaymentInternalResponse;
import dukku.common.shared.payment.out.PaymentApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 배치 기반 Settlement 생성 UseCase
 * - 스케줄에 따라 구매 확정된 주문 상품 조회 → Settlement 생성 (PENDING 상태)
 * - 배치에서 PENDING Settlement를 조회하여 예치금 충전
 * <p>
 * [아키텍처 원칙]
 * - Bounded Context 간 직접 참조 금지
 * - API Client를 통한 데이터 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateSettlementUseCase {

    private final SettlementSupport settlementSupport;
    private final DepositApiClient depositApiClient;
    private final OrderApiClient orderApiClient;
    private final PaymentApiClient paymentApiClient;

    @Value("${batch.settlement.fee-rate}")
    private BigDecimal feeRate;

    /**
     * 특정 기간의 구매 확정된 주문 상품들에 대해 정산 생성
     *
     * @param startDateTime 조회 시작 일시
     * @param endDateTime   조회 종료 일시
     * @return 생성된 Settlement 목록
     */
    @Transactional
    public List<Settlement> execute(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.info("[정산 생성] 배치 실행. 조회 기간: {} ~ {}", startDateTime, endDateTime);

        // 1. 구매 확정된 주문 상품 목록 조회
        List<ConfirmedOrderItemResponse> confirmedItems = orderApiClient.findConfirmedItems(startDateTime, endDateTime);

        log.info("[정산 생성] 구매 확정 주문 상품 조회 완료. 건수: {}", confirmedItems.size());

        // 2. 각 주문 상품에 대해 정산 생성
        List<Settlement> settlements = confirmedItems.stream()
                .map(this::createSettlement)
                .toList();

        log.info("[정산 생성 완료] 총 {}건 정산 생성됨", settlements.size());

        return settlements;
    }

    /**
     * 단일 주문 상품에 대한 정산 생성
     */
    private Settlement createSettlement(ConfirmedOrderItemResponse orderItem) {
        try {
            // 1. Payment 조회 - PaymentApiClient 사용 (주문에 대한 완료된 결제)
            PaymentInternalResponse payment = paymentApiClient.getPaymentByOrderUuid(orderItem.orderUuid());

            // 2. Deposit UUID 조회 - DepositApiClient 사용 (판매자의 예치금)
            UUID depositUuid = depositApiClient.getDepositUuid(orderItem.sellerUuid());

            // 3. 정산 예약일 계산 (구매 확정 후 당일 자정 넘어서 00:00)
            LocalDateTime reservationDate = SettlementSchedulePolicy.nextReservationDate();

            // 4. Settlement 생성
            Settlement settlement = Settlement.create(
                    orderItem.sellerUuid(),           // 판매자 UUID
                    orderItem.buyerUuid(),            // 구매자 UUID
                    payment.getPaymentUuid(),         // 결제 UUID
                    orderItem.orderUuid(),            // 주문 UUID
                    orderItem.orderItemUuid(),        // 주문 상품 UUID
                    depositUuid,                      // 예치금 UUID
                    Long.valueOf(orderItem.productPrice()), // 총액
                    feeRate,                          // 수수료율 (5%)
                    reservationDate                   // 정산 예약일 (구매 확정 후 당일 자정)
            );

            settlementSupport.save(settlement);

            log.debug("[정산 생성] settlementUuid={}, orderItemUuid={}, sellerUuid={}, amount={}",
                    settlement.getUuid(), orderItem.orderItemUuid(), orderItem.sellerUuid(), orderItem.productPrice());

            return settlement;

        } catch (Exception e) {
            log.error("[정산 생성 실패] orderItemUuid={}, error={}", orderItem.orderItemUuid(), e.getMessage(), e);
            throw e;
        }
    }
}
