package dukku.semicolon.boundedContext.deposit.in;

import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.common.shared.payment.event.RefundCompletedEvent;
import dukku.common.shared.settlement.event.SettlementDepositChargeRequestedEvent;
import dukku.semicolon.boundedContext.deposit.app.DepositFacade;
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
     * 결제 성공 시 예치금 차감 처리
     *
     * <p>
     * PaymentSuccessEvent 수신 시 예치금을 차감한다.
     * 차감 성공 시 DepositUsedEvent 발행.
     * 차감 실패 시 DepositDeductionFailedEvent 발행 (보상 트랜잭션 유도).
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentSuccessEvent event) {
        depositFacade.deductDepositForPayment(
                event.userUuid(),
                event.depositAmount(),
                event.orderUuid());
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
        depositFacade.refundDeposit(
                event.userUuid(),
                event.depositRefundAmount(),
                event.orderUuid());
    }

    /**
     * 정산 지급 요청 시 예치금 충전 처리
     *
     * <p>
     * SettlementPayoutRequestedEvent 수신 시 예치금을 충전한다.
     * 충전 성공 시 DepositChargeSucceededEvent 발행.
     * 충전 실패 시 DepositChargeFailedEvent 발행.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SettlementDepositChargeRequestedEvent command) {
        depositFacade.chargeDepositForSettlement(
                command.userUuid(),
                command.amount(),
                command.settlementUuid());
    }
}
