package dukku.semicolon.boundedContext.settlement.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.order.event.OrderItemConfirmedEvent;
import dukku.common.shared.settlement.event.SettlementCreateFailedEvent;
import dukku.common.shared.settlement.event.SettlementCreateSuccessEvent;
import dukku.semicolon.shared.deposit.out.depositApiClient.DepositApiClient;
// TODO: OrderApiClient, PaymentApiClient import 추가 예정
// import dukku.semicolon.shared.order.out.OrderApiClient;
// import dukku.semicolon.shared.payment.out.PaymentApiClient;
// import dukku.semicolon.shared.order.dto.OrderItemDto;
// import dukku.semicolon.shared.order.dto.OrderDto;
// import dukku.semicolon.shared.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 이벤트 기반 Settlement 생성 UseCase
 * - OrderItemConfirmedEvent 수신 → Settlement 생성 (PENDING 상태)
 * - 배치에서 PENDING Settlement를 조회하여 예치금 충전
 *
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
    private final EventPublisher eventPublisher;
    // TODO: API Client 추가 필요
    // private final OrderApiClient orderApiClient;
    // private final PaymentApiClient paymentApiClient;

    @Value("${batch.fee-rate}")
    private BigDecimal feeRate;

    @Transactional
    public void execute(OrderItemConfirmedEvent event) {
        log.info("[정산 생성] 구매 확정 이벤트 수신. orderItemUuid={}", event.orderItemUuid());

        try {
            // TODO: API Client를 통한 데이터 조회 (Bounded Context 간 직접 참조 금지)

            // 1. OrderItem 조회 - OrderApiClient 사용
            // OrderItemDto orderItem = orderApiClient.getOrderItem(event.orderItemUuid());

            // 2. Order 조회 - OrderApiClient 사용
            // OrderDto order = orderApiClient.getOrder(orderItem.getOrderUuid());

            // 3. Payment 조회 - PaymentApiClient 사용 (주문에 대한 완료된 결제)
            // PaymentDto payment = paymentApiClient.getPaymentByOrderUuid(order.getUuid());

            // 4. Deposit UUID 조회 - DepositApiClient 사용 (판매자의 예치금)
            // UUID depositUuid = depositApiClient.getDepositUuid(orderItem.getSellerUuid());

            // 5. 정산 예약일 계산 (구매 확정 후 당일 자정 넘어서 00:00)
            // LocalDateTime reservationDate = SettlementSchedulePolicy.nextReservationDate();

            // 6. Settlement 생성
            // Settlement settlement = Settlement.create(
            //         orderItem.getSellerUuid(),        // 판매자 UUID
            //         order.getBuyerUuid(),              // 구매자 UUID
            //         payment.getUuid(),                 // 결제 UUID
            //         order.getUuid(),                   // 주문 UUID
            //         orderItem.getUuid(),               // 주문 상품 UUID
            //         depositUuid,                       // 예치금 UUID
            //         orderItem.getProductPrice(),       // 총액
            //         feeRate,                           // 수수료율 (5%)
            //         reservationDate                    // 정산 예약일 (구매 확정 후 당일 자정)
            // );

            // settlementSupport.save(settlement);

            // log.info("[정산 생성 완료] settlementUuid={}, sellerUuid={}, amount={}, feeRate={}, reservationDate={}",
            //         settlement.getUuid(), orderItem.getSellerUuid(), orderItem.getProductPrice(), feeRate, reservationDate);

            // // 7. 성공 이벤트 발행
            // publishSuccessEvent();


            // ===========================================
            // TODO: 위의 API Client 코드로 교체 필요
            // ============================================
            log.warn("[임시] API Client 미구현으로 정산 생성 로직 스킵. OrderApiClient, PaymentApiClient 구현 필요");

        } catch (Exception e) {
            log.error("[정산 생성 실패] orderItemUuid={}, error={}", event.orderItemUuid(), e.getMessage(), e);
            publishFailureEvent();
            throw e;
        }
    }

    private void publishSuccessEvent() {
        // TODO: orderItem이 OrderItemDto로 변경되면 수정
        eventPublisher.publish(
                new SettlementCreateSuccessEvent()
        );
        log.info("[정산 생성 이벤트 발행]");
    }

    private void publishFailureEvent() {
        eventPublisher.publish(
                new SettlementCreateFailedEvent()
        );
        log.error("[정산 생성 실패 이벤트 발행]");
    }


}