package dukku.semicolon.boundedContext.settlement.in.batch.config;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.in.batch.listener.DepositChargeSkipListener;
import dukku.semicolon.boundedContext.settlement.in.batch.listener.SettlementBatchListener;
import dukku.semicolon.boundedContext.settlement.in.batch.processor.DepositChargeProcessor;
import dukku.semicolon.boundedContext.settlement.in.batch.writer.DepositChargeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementBatchProperties batchProperties;

    // Listeners
    private final SettlementBatchListener batchListener;
    private final DepositChargeSkipListener depositChargeSkipListener;

    // Step Components (예치금 충전)
    private final JpaPagingItemReader<Settlement> pendingSettlementReader;
    private final DepositChargeProcessor depositChargeProcessor;
    private final DepositChargeWriter depositChargeWriter;


    @Bean
    public Job settlementJob() {
        log.info("정산 배치 Job 생성");
        return new JobBuilder("settlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(batchListener)
                .start(depositChargeStep())
                .build();
    }

    /**
     * 예치금 충전 Step
     * - PENDING 상태의 Settlement 조회 (정산 예약일이 지난 건)
     * - 판매자 예치금에 정산금액 충전
     * - Settlement 상태를 SUCCESS로 변경
     * 
     * Skip 정책:
     * - SettlementValidationException: 정산 데이터 유효성 검증 실패 (금액 오류, 상태 전이 불가 등)
     * - SettlementProcessingException: 정산 처리 실패 (예치금 계좌 없음, 충전 실패 등)
     * 
     * Retry 정책:
     * - DataAccessException: DB 연결 끊김, 데드락
     */
    @Bean
    public Step depositChargeStep() {
        log.info("예치금 충전 Step 생성 - chunkSize: {}, skipLimit: {}, retryLimit: {}",
                batchProperties.getChunkSize(),
                batchProperties.getSkipLimit(),
                batchProperties.getRetryLimit());

        return new StepBuilder("depositChargeStep", jobRepository)
                .<Settlement, Settlement>chunk(batchProperties.getChunkSize(), transactionManager)
                .reader(pendingSettlementReader)
                .processor(depositChargeProcessor)
                .writer(depositChargeWriter)
                // Skip 정책
                .faultTolerant()
                .skip(SettlementValidationException.class)
                .skip(SettlementProcessingException.class)
                .skipLimit(batchProperties.getSkipLimit())
                // Retry 정책
                .retry(DataAccessException.class)
                .retryLimit(batchProperties.getRetryLimit())
                // Listener
                .listener(batchListener)
                .listener(depositChargeSkipListener)
                .build();
    }
}
