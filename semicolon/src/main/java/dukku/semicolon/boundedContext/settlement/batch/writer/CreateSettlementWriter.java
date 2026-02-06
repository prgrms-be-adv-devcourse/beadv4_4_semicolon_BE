package dukku.semicolon.boundedContext.settlement.batch.writer;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Step 1: 생성된 Settlement 저장 Writer
 * - PENDING 상태의 Settlement 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateSettlementWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void write(Chunk<? extends Settlement> chunk) throws Exception {
        log.info("[Step 1 Writer] PENDING 상태 Settlement 저장 시작 - 건수: {}", chunk.size());

        for (Settlement settlement : chunk) {
            settlementRepository.save(settlement);
            log.debug("[Step 1 Writer] Settlement 저장 완료 - UUID: {}, orderItemUuid: {}, 상태: {}",
                    settlement.getUuid(),
                    settlement.getOrderItemId(),
                    settlement.getSettlementStatus());
        }

        log.info("[Step 1 Writer] PENDING 상태 Settlement 저장 완료 - 총 {}건 생성됨", chunk.size());
    }
}
