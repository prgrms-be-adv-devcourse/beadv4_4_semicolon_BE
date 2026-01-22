package dukku.semicolon.boundedContext.settlement.in.batch.reader;

import dukku.semicolon.boundedContext.settlement.in.batch.config.SettlementBatchProperties;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * PENDING 상태의 Settlement를 조회하는 Reader
 * - 정산 예약일이 현재 시간 이전인 건만 조회
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DepositChargeReader {

    private final EntityManagerFactory entityManagerFactory;
    private final SettlementBatchProperties batchProperties;


    @Bean
    public JpaPagingItemReader<Settlement> pendingSettlementReader() {
        String jpql = """
                SELECT s FROM Settlement s
                WHERE s.settlementStatus = :status
                AND s.settlementReservationDate <= :now
                ORDER BY s.settlementReservationDate ASC
                """;

        log.info("예치금 충전 대상 Settlement Reader 생성 - pageSize: {}", batchProperties.getPageSize());

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("pendingSettlementReader")
                .entityManagerFactory(entityManagerFactory) // JPA Paging Reader는 내부적으로 EntityManager를 사용
                .queryString(jpql)
                .parameterValues(Map.of(
                        "status", SettlementStatus.PENDING,
                        "now", LocalDateTime.now()
                ))
                .pageSize(batchProperties.getPageSize())
                .saveState(true)
                .build();
    }
}
