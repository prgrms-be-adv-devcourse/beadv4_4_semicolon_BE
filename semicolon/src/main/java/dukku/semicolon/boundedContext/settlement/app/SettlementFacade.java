package dukku.semicolon.boundedContext.settlement.app;

import dukku.common.shared.deposit.event.DepositChargeFailedEvent;
import dukku.common.shared.deposit.event.DepositChargeSucceededEvent;
import dukku.common.shared.order.event.OrderItemConfirmedEvent;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.dto.SettlementDetailResponse;
import dukku.semicolon.shared.settlement.dto.SettlementSearchCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementFacade {

    private final GetSettlementUseCase getSettlementUseCase;
    private final GetSettlementListUseCase getSettlementListUseCase;
    private final GetSettlementStatisticsUseCase getSettlementStatisticsUseCase;
    private final CreateSettlementUseCase createSettlementUseCase;
    private final SettlementSupport settlementSupport;
    private final RequestDepositChargeUseCase requestDepositChargeUseCase;

    @Transactional(readOnly = true)
    public SettlementDetailResponse getSettlement(UUID settlementUuid) {
        Settlement settlement = getSettlementUseCase.execute(settlementUuid);
        return SettlementDetailResponse.from(settlement);
    }

    @Transactional(readOnly = true)
    public Page<SettlementDetailResponse> getSettlements(SettlementSearchCondition condition, Pageable pageable) {
        Page<Settlement> settlements = getSettlementListUseCase.execute(condition, pageable);
        return settlements.map(SettlementDetailResponse::from);
    }

    @Transactional(readOnly = true)
    public SettlementStatisticsResponse getStatistics(SettlementStatisticsCondition condition) {
        return getSettlementStatisticsUseCase.execute(condition);
    }

    public void createSettlement(OrderItemConfirmedEvent event) {
        createSettlementUseCase.execute(event);
    }

    public Settlement requestDepositCharge(Settlement settlement) {
        return requestDepositChargeUseCase.execute(settlement);
    }

    public void completeSettlement(DepositChargeSucceededEvent event) {
        Settlement settlement = settlementSupport.findByUuid(event.settlementUuid());
        settlement.complete();
        settlementSupport.save(settlement);
    }

    public void failSettlement(DepositChargeFailedEvent event) {
        Settlement settlement = settlementSupport.findByUuid(event.settlementUuid());
        settlement.fail();
        settlementSupport.save(settlement);
    }
}
