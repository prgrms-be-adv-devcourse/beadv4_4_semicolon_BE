package dukku.semicolon.boundedContext.payment.out;

import dukku.common.shared.order.event.PaymentRollbackRequestEvent;
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
 * Payment 도메인 이벤트 리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final RefundPaymentUseCase refundPaymentUseCase;
    private final PaymentSupport paymentSupport;

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

                refundPaymentUseCase.execute(refundRequest, "rollback-" + payment.getUuid());
                log.info("[결제 롤백] 환불 성공. paymentUuid={}", payment.getUuid());
            } catch (Exception e) {
                log.error("[결제 롤백] 환불 실패! 수동 조치 필요. paymentUuid={}, error={}", 
                        payment.getUuid(), e.getMessage());
            }
        }
    }
}
