package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class GetSettlementUseCase {

    private final SettlementSupport settlementSupport;

    @Transactional(readOnly = true)
    public Settlement execute(UUID settlementUuid) {
        return settlementSupport.findByUuid(settlementUuid);
    }
}
