package dukku.semicolon.boundedContext.settlement.in.batch.processor;

import dukku.semicolon.boundedContext.settlement.app.SettlementFacade;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargeProcessor implements ItemProcessor<Settlement, Settlement> {

    private final SettlementFacade settlementFacade;

    @Override
    public Settlement process(Settlement settlement) throws Exception {
        // TODO: 예치금 충전 요청
        return settlementFacade.requestDepositCharge(settlement);
    }
}
