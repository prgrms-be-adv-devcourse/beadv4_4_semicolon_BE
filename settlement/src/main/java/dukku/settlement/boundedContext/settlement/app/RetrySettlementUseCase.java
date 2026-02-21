package dukku.settlement.boundedContext.settlement.app;

import dukku.settlement.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 실패한 정산 재처리 UseCase
 * - Settlement 상태: FAILED → PENDING
 * - 관리자가 실패한 정산을 수동으로 재처리 대기 상태로 변경
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetrySettlementUseCase {

    private final SettlementSupport settlementSupport;

    @Transactional
    public Settlement execute(UUID settlementUuid) {
        log.info("[정산 재처리] settlementUuid={}", settlementUuid);

        Settlement settlement = settlementSupport.findByUuid(settlementUuid);
        settlement.retry();
        settlementSupport.save(settlement);

        log.info("[정산 재처리 완료] settlementUuid={}, newStatus={}",
                settlementUuid, settlement.getSettlementStatus());

        return settlement;
    }
}
