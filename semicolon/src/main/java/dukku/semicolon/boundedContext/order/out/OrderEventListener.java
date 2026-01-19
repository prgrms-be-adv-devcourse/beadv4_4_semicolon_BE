package dukku.semicolon.boundedContext.order.out;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.order.event.PaymentRollbackRequestEvent;
import dukku.common.shared.payment.event.PaymentFailEvent;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.semicolon.boundedContext.order.app.UpdateOrderRefundStatusUseCase;
import dukku.semicolon.boundedContext.order.app.UpdateOrderStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final UpdateOrderRefundStatusUseCase updateOrderRefundStatusUseCase;
    private final EventPublisher eventPublisher;

    /* TODO: 환불은 세미 프로젝트에서 고려안함. 최종 프로젝트에서 환불 적용.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(RefundCompletedEvent event) {
        updateOrderRefundStatusUseCase.updateRefund(event.orderUuid(), event.status(), event.refundAmount());
    }*/

    /**결제서비스에서 사용.
     * 결제 완료 시 주문 상태 및 해당 주문 상품 상태 변경.
     * retry 실패 할 경우 로그를 남기며 보상 트랜잭션 실행
     * TODO: 최종 프로젝트에서 환불 적용.
     */
    @Async
    @Retryable(backoff = @Backoff(delay = 1000))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentSuccessEvent event) {
        updateOrderStatusUseCase.confirmPayment(event.orderUuid());
    }

    @Recover
    public void recoverSuccess(Exception e, PaymentSuccessEvent event) {
        log.error("[치명적 오류] 결제는 성공했으나 주문 처리에 실패했습니다. 자동 환불을 요청합니다. orderUuid={}, error={}",
                event.orderUuid(), e.getMessage());

        eventPublisher.publish(
                new PaymentRollbackRequestEvent(
                        event.orderUuid(),
                        "주문 시스템 오류로 인한 자동 환불 처리"
                )
        );
    }


    /**결제서비스에서 사용.
     * 결제 완료 시 주문 상태 및 해당 주문 상품 상태 변경.
     * retry 실패 할 경우 로그를 남기며 보상 트랜잭션 실행
     * TODO: 최종 프로젝트에서 환불 적용.
     */
    @Async
    @Retryable(backoff = @Backoff(delay = 1000))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentFailEvent event) {
        updateOrderStatusUseCase.failPayment(event.orderUuid());
    }

    @Recover
    public void recoverFail(Exception e, PaymentFailEvent event) {
        log.error("[치명적 오류] 결제 실패 처리를 DB에 반영하지 못했습니다! 수동 조치 필요. orderUuid={}", event.orderUuid(), e);
    }
}
