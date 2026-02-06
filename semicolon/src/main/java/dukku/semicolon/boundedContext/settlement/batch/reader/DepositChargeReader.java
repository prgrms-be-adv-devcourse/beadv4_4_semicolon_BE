package dukku.semicolon.boundedContext.settlement.batch.reader;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import dukku.semicolon.boundedContext.settlement.batch.config.SettlementBatchProperties;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Step 3: 예치금 충전 대상 Settlement Reader
 * - PROCESSING 상태의 Settlement 조회
 * - Step 2에서 금액 검증 완료된 건만 대상
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DepositChargeReader {

    private final EntityManagerFactory entityManagerFactory;
    private final SettlementBatchProperties batchProperties;

    @Bean(destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<Settlement> processingSettlementReader() {
        String jpql = """
                SELECT s FROM Settlement s
                WHERE s.settlementStatus = :status
                ORDER BY s.settlementReservationDate ASC
                """;

        log.info("[Step 3] 예치금 충전 대상 Settlement Reader 생성 - pageSize: {}", batchProperties.getPageSize());

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("processingSettlementReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "status", SettlementStatus.PROCESSING
                ))
                .pageSize(batchProperties.getPageSize())
                .saveState(true)
                .build();
    }
}
