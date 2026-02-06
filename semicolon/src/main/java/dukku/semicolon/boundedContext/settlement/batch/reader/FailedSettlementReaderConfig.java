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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 재처리 대상 Settlement Reader
 * - FAILED 상태의 Settlement 조회
 * - 1시간 전에 실패한 건들을 재처리하기 위함
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FailedSettlementReaderConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final SettlementBatchProperties batchProperties;

    @Bean(destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<Settlement> failedSettlementReader(
            @Value("#{jobParameters['now']}") LocalDateTime now
    ) {
        String jpql = """
                SELECT s FROM Settlement s
                WHERE s.settlementStatus = :status
                ORDER BY s.updatedAt ASC
                """;

        log.info("[Retry] FAILED Settlement Reader 생성 - pageSize: {}, now: {}",
                batchProperties.getPageSize(), now);

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("failedSettlementReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpql)
                .parameterValues(Map.of(
                        "status", SettlementStatus.FAILED
                ))
                .pageSize(batchProperties.getPageSize())
                .saveState(true)
                .build();
    }
}
