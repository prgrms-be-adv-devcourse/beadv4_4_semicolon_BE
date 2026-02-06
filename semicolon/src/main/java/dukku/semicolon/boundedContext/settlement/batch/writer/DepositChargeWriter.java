package dukku.semicolon.boundedContext.settlement.batch.writer;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Step 2: 예치금 충전 완료된 Settlement 저장 Writer
 * - SUCCESS 상태로 변경된 Settlement 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargeWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void write(Chunk<? extends Settlement> chunk) throws Exception {
        log.info("[Step 2 Writer] SUCCESS 상태 Settlement 저장 시작 - 건수: {}", chunk.size());

        for (Settlement settlement : chunk) {
            settlementRepository.save(settlement);
            log.debug("[Step 2 Writer] Settlement 상태 업데이트 완료 - UUID: {}, 상태: {}",
                    settlement.getUuid(),
                    settlement.getSettlementStatus());
        }

        log.info("[Step 2 Writer] SUCCESS 상태 Settlement 저장 완료 - 총 {}건 처리됨", chunk.size());
    }
}
