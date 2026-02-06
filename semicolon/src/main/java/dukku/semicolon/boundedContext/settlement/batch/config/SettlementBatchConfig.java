package dukku.semicolon.boundedContext.settlement.batch.config;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.batch.listener.DepositChargeSkipListener;
import dukku.semicolon.boundedContext.settlement.batch.listener.SettlementBatchListener;
import dukku.semicolon.boundedContext.settlement.batch.processor.CreateSettlementProcessor;
import dukku.semicolon.boundedContext.settlement.batch.processor.DepositChargeProcessor;
import dukku.semicolon.boundedContext.settlement.batch.processor.ValidateSettlementProcessor;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import dukku.semicolon.boundedContext.settlement.batch.writer.CreateSettlementWriter;
import dukku.semicolon.boundedContext.settlement.batch.writer.DepositChargeWriter;
import dukku.semicolon.boundedContext.settlement.batch.writer.ValidateSettlementWriter;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import dukku.semicolon.boundedContext.settlement.batch.processor.RetrySettlementProcessor;
import dukku.semicolon.boundedContext.settlement.batch.writer.RetrySettlementWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;

/**
 * 정산 배치 설정 (3-Step 구조)
 *
 * <pre>
 * Job: settlementJob
 *  ├─ Step 1: createSettlementStep (정산 대상 생성)
 *  │   - Order BC API 호출 → 당일 확정된 OrderItem 조회 → Settlement 생성
 *  │
 *  ├─ Step 2: validateSettlementStep (금액 검증)
 *  │   - PENDING Settlement 조회 → 금액 검증 → PROCESSING 상태
 *  │
 *  └─ Step 3: depositChargeStep (예치금 충전)
 *      - PROCESSING Settlement 조회 → Deposit API 동기 호출 → SUCCESS 상태
 * </pre>
 *
 * [Step 분리 이유]
 * 1. 정산 대상 명확화 (Step 1에서 Settlement 생성 = 스냅샷 역할)
 * 2. 금액 검증과 실제 충전 분리 (책임 분리)
 * 3. 재시작 시 실패한 Step부터 재실행 가능
 * 4. 중복 정산 방지 (Idempotency)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final SettlementBatchProperties batchProperties;

    // Listeners
    private final SettlementBatchListener batchListener;
    private final DepositChargeSkipListener depositChargeSkipListener;

    // Step 1: 정산 대상 생성
    private final ListItemReader<ConfirmedOrderItemResponse> confirmedOrderItemReader;
    private final CreateSettlementProcessor createSettlementProcessor;
    private final CreateSettlementWriter createSettlementWriter;

    // Step 2: 금액 검증
    private final JpaPagingItemReader<Settlement> pendingSettlementForValidationReader;
    private final ValidateSettlementProcessor validateSettlementProcessor;
    private final ValidateSettlementWriter validateSettlementWriter;

    // Step 3: 예치금 충전
    private final JpaPagingItemReader<Settlement> processingSettlementReader;
    private final DepositChargeProcessor depositChargeProcessor;
    private final DepositChargeWriter depositChargeWriter;

    // Retry: 재처리
    private final JpaPagingItemReader<Settlement> failedSettlementReader;
    private final RetrySettlementProcessor retrySettlementProcessor;
    private final RetrySettlementWriter retrySettlementWriter;


    /**
     * 정산 배치 Job
     * - Step 1 (정산 대상 생성) → Step 2 (금액 검증) → Step 3 (예치금 충전)
     */
    @Bean
    public Job settlementJob() {
        log.info("정산 배치 Job 생성 (3-Step 구조)");
        return new JobBuilder("settlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(batchListener)
                .start(createSettlementStep())   // Step 1: 정산 대상 생성
                .next(validateSettlementStep())  // Step 2: 금액 검증
                .next(depositChargeStep())       // Step 3: 예치금 충전
                .build();
    }


    /**
     * 정산 재처리 배치 Job
     * - 1시간 전 실패한 정산 건들을 재처리
     * - Retry Step (재처리: FAILED → PENDING) → Step 2 (금액 검증) → Step 3 (예치금 충전)
     */
    @Bean
    public Job settlementRetryJob() {
        log.info("정산 재처리 배치 Job 생성 (3-Step 구조)");
        return new JobBuilder("settlementRetryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(batchListener)
                .start(retrySettlementStep())    // Retry Step: FAILED → PENDING
                .next(validateSettlementStep())  // Step 2: 금액 검증 (재사용)
                .next(depositChargeStep())       // Step 3: 예치금 충전 (재사용)
                .build();
    }


    /**
     * Step 1: 정산 대상 생성
     * - Order BC API 호출 → 당일(어제) 확정된 OrderItem 조회
     * - Settlement 생성 (PENDING 상태)
     */
    @Bean
    public Step createSettlementStep() {
        log.info("[Step 1] 정산 대상 생성 Step 생성 - chunkSize: {}", batchProperties.getChunkSize());

        return new StepBuilder("createSettlementStep", jobRepository)
                .<ConfirmedOrderItemResponse, Settlement>chunk(batchProperties.getChunkSize())
                .reader(confirmedOrderItemReader)
                .processor(createSettlementProcessor)
                .writer(createSettlementWriter)
                // Skip 정책
                .faultTolerant()
                .skip(SettlementProcessingException.class)
                .skipLimit(batchProperties.getSkipLimit())
                // Retry 정책
                .retry(DataAccessException.class)
                .retryLimit(batchProperties.getRetryLimit())
                // Listener
                .listener(batchListener)
                .build();
    }


    /**
     * Step 2: 금액 검증
     * - PENDING 상태의 Settlement 조회 (정산 예약일 <= 현재 시간)
     * - 금액 유효성 검증
     * - PENDING → PROCESSING 상태 전이
     */
    @Bean
    public Step validateSettlementStep() {
        log.info("[Step 2] 금액 검증 Step 생성 - chunkSize: {}, skipLimit: {}",
                batchProperties.getChunkSize(),
                batchProperties.getSkipLimit());

        return new StepBuilder("validateSettlementStep", jobRepository)
                .<Settlement, Settlement>chunk(batchProperties.getChunkSize())
                .reader(pendingSettlementForValidationReader)
                .processor(validateSettlementProcessor)
                .writer(validateSettlementWriter)
                // Skip 정책
                .faultTolerant()
                .skip(SettlementValidationException.class)
                .skipLimit(batchProperties.getSkipLimit())
                // Retry 정책
                .retry(DataAccessException.class)
                .retryLimit(batchProperties.getRetryLimit())
                // Listener
                .listener(batchListener)
                .listener(depositChargeSkipListener)
                .build();
    }


    /**
     * Step 3: 예치금 충전
     * - PROCESSING 상태의 Settlement 조회
     * - Deposit BC API Client를 통한 예치금 충전 (동기 방식)
     * - PROCESSING → SUCCESS 상태 전이
     *
     * [Idempotency]
     * - 이미 SUCCESS/FAILED 상태인 건은 Processor에서 Skip (null 반환)
     */
    @Bean
    public Step depositChargeStep() {
        log.info("[Step 3] 예치금 충전 Step 생성 - chunkSize: {}, skipLimit: {}, retryLimit: {}",
                batchProperties.getChunkSize(),
                batchProperties.getSkipLimit(),
                batchProperties.getRetryLimit());

        return new StepBuilder("depositChargeStep", jobRepository)
                .<Settlement, Settlement>chunk(batchProperties.getChunkSize())
                .reader(processingSettlementReader)
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


    /**
     * Retry Step: 재처리
     * - FAILED 상태의 Settlement 조회
     * - FAILED → PENDING 상태 전이
     * - 이후 validateSettlementStep, depositChargeStep에서 정상 플로우 진행
     */
    @Bean
    public Step retrySettlementStep() {
        log.info("[Retry Step] 재처리 Step 생성 - chunkSize: {}, skipLimit: {}",
                batchProperties.getChunkSize(),
                batchProperties.getSkipLimit());

        return new StepBuilder("retrySettlementStep", jobRepository)
                .<Settlement, Settlement>chunk(batchProperties.getChunkSize())
                .reader(failedSettlementReader)
                .processor(retrySettlementProcessor)
                .writer(retrySettlementWriter)
                // Skip 정책
                .faultTolerant()
                .skip(SettlementValidationException.class)
                .skipLimit(batchProperties.getSkipLimit())
                // Retry 정책
                .retry(DataAccessException.class)
                .retryLimit(batchProperties.getRetryLimit())
                // Listener
                .listener(batchListener)
                .build();
    }
}
