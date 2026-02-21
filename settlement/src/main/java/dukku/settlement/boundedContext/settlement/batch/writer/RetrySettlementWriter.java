package dukku.settlement.boundedContext.settlement.batch.writer;

import dukku.settlement.boundedContext.settlement.app.SettlementMetrics;
import dukku.settlement.boundedContext.settlement.entity.Settlement;
import dukku.settlement.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 재처리 Settlement 저장 Writer
 * - PENDING 상태로 복구된 Settlement 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetrySettlementWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;
    private final SettlementMetrics settlementMetrics;

    @Override
    public void write(Chunk<? extends Settlement> chunk) throws Exception {
        log.info("[Retry Writer] PENDING 상태로 복구된 Settlement 저장 시작 - 건수: {}", chunk.size());

        for (Settlement settlement : chunk) {
            settlementRepository.save(settlement);

            // 메트릭 기록: 재처리 건수
            settlementMetrics.incrementRetry();

            log.debug("[Retry Writer] Settlement 상태 업데이트 완료 - UUID: {}, 상태: {}",
                    settlement.getUuid(),
                    settlement.getSettlementStatus());
        }

        log.info("[Retry Writer] PENDING 상태로 복구된 Settlement 저장 완료 - 총 {}건 처리됨", chunk.size());
    }
}
