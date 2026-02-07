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
import java.util.HashMap;
import java.util.Map;

/**
 * Step 2: 금액 검증 대상 Settlement Reader
 * - PENDING 상태의 Settlement 조회
 * - 정산 예약일이 현재 시간 이전인 건만 조회
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ValidateSettlementReader {

    private final EntityManagerFactory entityManagerFactory;
    private final SettlementBatchProperties batchProperties;

    @Bean(destroyMethod = "")
    @StepScope
    public JpaPagingItemReader<Settlement> pendingSettlementForValidationReader(
            @Value("#{jobParameters['now']}") LocalDateTime now
    ) {
        // null인 경우 현재 시간 사용 (Spring Batch 6.0 타입 변환 이슈 대응)
        LocalDateTime effectiveNow = now != null ? now : LocalDateTime.now();

        String jpql = """
                SELECT s FROM Settlement s
                WHERE s.settlementStatus = :status
                AND s.settlementReservationDate <= :now
                ORDER BY s.settlementReservationDate ASC
                """;

        log.info("[Step 2] 금액 검증 대상 Settlement Reader 생성 - pageSize: {}, now: {}",
                batchProperties.getPageSize(), effectiveNow);

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("status", SettlementStatus.PENDING);
        parameterValues.put("now", effectiveNow);

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("pendingSettlementForValidationReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpql)
                .parameterValues(parameterValues)
                .pageSize(batchProperties.getPageSize())
                .saveState(true)
                .build();
    }
}
