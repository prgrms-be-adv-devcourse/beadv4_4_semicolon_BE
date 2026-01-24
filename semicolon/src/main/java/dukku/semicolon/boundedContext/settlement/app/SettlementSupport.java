package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import dukku.semicolon.shared.settlement.exception.SettlementNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementSupport {
    private final SettlementRepository settlementRepository;

    public Settlement findByUuid(UUID settlementUuid) {
        return settlementRepository.findByUuid(settlementUuid)
                .orElseThrow(SettlementNotFoundException::new);
    }

    public Settlement save(Settlement settlement) {
        return settlementRepository.save(settlement);
    }
}
