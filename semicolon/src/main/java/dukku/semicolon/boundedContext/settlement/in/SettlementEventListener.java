package dukku.semicolon.boundedContext.settlement.in;

import dukku.common.shared.deposit.event.DepositChargeFailedEvent;
import dukku.common.shared.deposit.event.DepositChargeSucceededEvent;
import dukku.common.shared.order.event.OrderItemConfirmedEvent;
import dukku.semicolon.boundedContext.settlement.app.SettlementFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;


@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventListener {

    private final SettlementFacade settlementFacade;

    /**
     * 구매 확정 이벤트 핸들러
     * - OrderItem 구매 확정 시 Settlement 생성 (PENDING 상태)
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(OrderItemConfirmedEvent event) {
        log.info("[정산 이벤트] 구매 확정 이벤트 수신. orderItemUuid={}", event.orderItemUuid());
        settlementFacade.createSettlement(event);
    }

    /**
     * 예치금 충전 성공 이벤트 핸들러
     * - Settlement 상태를 SUCCESS로 변경
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(DepositChargeSucceededEvent event) {
        log.info("[정산 이벤트] 예치금 충전 성공 이벤트 수신. settlementUuid={}", event.settlementUuid());
        settlementFacade.completeSettlement(event);
    }

    /**
     * 예치금 충전 실패 이벤트 핸들러
     * - Settlement 상태를 FAILED로 변경
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(DepositChargeFailedEvent event) {
        log.error("[정산 이벤트] 예치금 충전 실패 이벤트 수신. settlementUuid={}, reason={}",
                event.settlementUuid(), event.reason());
        settlementFacade.failSettlement(event);
    }

}
