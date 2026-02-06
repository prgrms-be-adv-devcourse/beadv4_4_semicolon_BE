package dukku.semicolon.boundedContext.settlement.batch.processor;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.SettlementSchedulePolicy;
import dukku.semicolon.boundedContext.settlement.batch.config.SettlementBatchProperties;
import dukku.semicolon.shared.deposit.out.depositApiClient.DepositApiClient;
import dukku.semicolon.shared.payment.dto.PaymentInternalResponse;
import dukku.semicolon.shared.payment.out.PaymentApiClient;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Step 1: 정산 대상 생성 Processor
 * - 구매 확정된 OrderItem → Settlement 변환
 * - PENDING 상태로 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateSettlementProcessor implements ItemProcessor<ConfirmedOrderItemResponse, Settlement> {

    private final DepositApiClient depositApiClient;
    private final PaymentApiClient paymentApiClient;
    private final SettlementBatchProperties batchProperties;

    @Override
    public Settlement process(ConfirmedOrderItemResponse orderItem) throws Exception {
        log.debug("[Step 1 Processor] Settlement 생성 시작. orderItemUuid={}", orderItem.orderItemUuid());

        try {
            // 1. 판매자의 예치금 계좌 UUID 조회
            UUID depositUuid = depositApiClient.getDepositUuid(orderItem.sellerUuid());

            if (depositUuid == null) {
                log.error("[Step 1 Processor] 판매자 예치금 계좌 없음. sellerUuid={}", orderItem.sellerUuid());
                throw SettlementProcessingException.depositAccountNotFound(String.valueOf(orderItem.sellerUuid()));
            }

            // 2. Payment 정보 조회
            PaymentInternalResponse payment = paymentApiClient.getPaymentByOrderUuid(orderItem.orderUuid());
            UUID paymentUuid = payment.getPaymentUuid();

            if (paymentUuid == null) {
                log.error("[Step 1 Processor] 결제 정보 없음. orderUuid={}", orderItem.orderUuid());
                throw new SettlementProcessingException("결제 정보를 찾을 수 없습니다. orderUuid=" + orderItem.orderUuid());
            }

            // 3. Settlement 생성 (PENDING 상태)
            Settlement settlement = Settlement.create(
                    orderItem.sellerUuid(),             // 판매자 UUID
                    orderItem.buyerUuid(),              // 구매자 UUID
                    paymentUuid,                        // 결제 UUID
                    orderItem.orderUuid(),              // 주문 UUID
                    orderItem.orderItemUuid(),          // 주문 상품 UUID
                    depositUuid,                        // 예치금 계좌 UUID
                    (long) orderItem.productPrice(),    // 총액 (상품 가격)
                    batchProperties.getFeeRate(),       // 수수료율
                    SettlementSchedulePolicy.nextReservationDate() // 정산 예약일
            );

            log.info("[Step 1 Processor] Settlement 생성 완료. settlementUuid={}, orderItemUuid={}, amount={}",
                    settlement.getUuid(), orderItem.orderItemUuid(), orderItem.productPrice());

            return settlement;

        } catch (SettlementProcessingException e) {
            log.error("[Step 1 Processor-Skip] Settlement 생성 실패. orderItemUuid={}, error={}",
                    orderItem.orderItemUuid(), e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[Step 1 Processor-Skip] 예상치 못한 오류. orderItemUuid={}, error={}",
                    orderItem.orderItemUuid(), e.getMessage(), e);
            throw new SettlementProcessingException(
                    "Settlement 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
