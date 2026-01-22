package dukku.semicolon.boundedContext.settlement.in;

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

    /*
    TODO: order, deposit이벤트 필요
     */

//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(OrderItemConfirmedEvent event) {
//        log.info("구매 확정 이벤트 수신");
//        settlementFacade.createSettlement(event);
//    }
//
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(DepositChargeSuccedEvent event) {
//        log.info("예치금 충전 성공 이벤트 수신");
//        settlementFacade.completeSettlement(event);
//    }
//
//    @TransactionalEventListener(phase = AFTER_COMMIT)
//    @Transactional(propagation = REQUIRES_NEW)
//    public void handle(DepositChargeFailedEvent event) {
//        log.error("예치금 충전 실패 이벤트 수신");
//        settlementFacade.failSettlement(event);
//    }

}
