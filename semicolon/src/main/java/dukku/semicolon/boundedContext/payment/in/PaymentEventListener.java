package dukku.semicolon.boundedContext.payment.in;

import dukku.common.shared.deposit.event.DepositDeductionFailedEvent;
import dukku.common.shared.order.event.PaymentRollbackRequestEvent;
import dukku.semicolon.boundedContext.payment.app.CompensatePaymentUseCase;
import dukku.semicolon.boundedContext.payment.app.PaymentFacade;
import dukku.semicolon.boundedContext.payment.app.PaymentSupport;
import dukku.semicolon.boundedContext.payment.app.RefundPaymentUseCase;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 결제 도메인 이벤트 리스너 (Inbound Adapter)
 * 
 * <p>
 * 외부 시스템(주문, 예치금 등)에서 발생한 이벤트를 수신하여 결제 도메인 로직을 구동한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentFacade paymentFacade;
    private final PaymentSupport paymentSupport;

    /**
     * 예치금 차감 실패 시 보상 트랜잭션(결제 취소) 처리
     * 
     * <p>
     * DepositDeductionFailedEvent 수신 시 이미 승인된 PG 결제를 취소하여 데이터 일관성을 유지함.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DepositDeductionFailedEvent event) {
        log.warn("[결제 보상 트랜잭션 시작] 예치금 차감 실패 감지: orderUuid={}, reason={}",
                event.orderUuid(), event.reason());

        paymentFacade.compensatePayment(event.orderUuid(), event.reason());
    }

    /**
     * 주문 처리 실패 시 결제 롤백(자동 환불) 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentRollbackRequestEvent event) {
        log.info("[결제 롤백] 주문 처리 실패로 인한 자동 환불 시작. orderUuid={}, 사유={}",
                event.orderUuid(), event.reason());

        // 해당 주문에 대한 완료된 결제 조회
        List<Payment> payments = paymentSupport.findPaymentsByOrderUuid(event.orderUuid());

        for (Payment payment : payments) {
            try {
                PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                        .paymentId(payment.getUuid())
                        .refundAmount(payment.getAmount() - payment.getRefundTotal())
                        .reason(event.reason())
                        .build();

                // Idempotency Key는 내부 롤백이므로 랜덤 생성 혹은 Prefix 사용
                paymentFacade.refundPayment(refundRequest, "rollback-" + payment.getUuid());
                log.info("[결제 롤백] 환불 성공. paymentUuid={}", payment.getUuid());
            } catch (Exception e) {
                log.error("[결제 롤백] 환불 실패! 수동 조치 필요. paymentUuid={}, error={}",
                        payment.getUuid(), e.getMessage());
            }
        }
    }
}
