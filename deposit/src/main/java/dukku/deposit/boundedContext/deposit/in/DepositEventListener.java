package dukku.deposit.boundedContext.deposit.in;

import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.common.shared.payment.event.RefundCompletedEvent;
import dukku.common.shared.settlement.event.SettlementDepositChargeRequestedEvent;
import dukku.deposit.boundedContext.deposit.app.ChargeDepositForSettlementUseCase;
import dukku.deposit.boundedContext.deposit.app.DepositFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositEventListener {

    private final DepositFacade depositFacade;

    /**
     * 결제 완료 시 예치금 차감 라이프사이클 처리
     *
     * <p>
     * 결제 트랜잭션이 최종 커밋된 후(AFTER_COMMIT), 비동기적으로 예치금 차감 프로세스를 시작한다.
     * 상품별 사용 상세 내역(itemDepositUsages)을 포함하여 파사드에 위임한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentSuccessEvent event) {
        // paymentUuid 전달 (보상 트랜잭션 식별)
        depositFacade.deductDepositForPayment(
                event.userUuid(),
                event.paymentDeposit(),
                event.orderUuid(),
                event.paymentUuid(),
                event.itemDepositUsages());
        depositFacade.increaseSystemDepositForPg(event.orderUuid(), event.pgAmount());
    }

    /**
     * 환불 완료 시 예치금 복구 처리
     *
     * <p>
     * RefundCompletedEvent 수신 시 예치금을 롤백(재적립)한다.
     * 복구 성공 시 DepositRefundedEvent 발행.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RefundCompletedEvent event) {
        // paymentId 전달 (예치금 롤백 실패 연계)
        depositFacade.refundDeposit(
                event.userUuid(),
                event.refundDepositAmount(),
                event.orderUuid(),
                event.paymentId());
    }

    /**
     * 정산 지급 요청 시 예치금 충전 처리
     *
     * <p>
     * SettlementPayoutRequestedEvent 수신 시 예치금을 충전한다.
     * 충전 성공 시 DepositChargeSucceededEvent 발행.
     * 충전 실패 시 DepositChargeFailedEvent 발행.
     *
     * @deprecated 정산 예치금 충전은 이제 Internal API 호출을 통한
     *             {@link ChargeDepositForSettlementUseCase} 사용을 권장합니다.
     *             이벤트 기반 방식은 하위 호환성을 위해 유지되나, 향후 제거될 예정입니다.
     */
    @Deprecated
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SettlementDepositChargeRequestedEvent command) {
        log.warn("[DEPRECATED] 이벤트 기반 정산 충전 요청이 수신되었습니다. API 방식으로의 전환이 필요합니다. settlementUuid={}",
                command.settlementUuid());
        depositFacade.chargeDepositForSettlement(
                command.userUuid(),
                command.amount(),
                command.settlementUuid());
    }
}
