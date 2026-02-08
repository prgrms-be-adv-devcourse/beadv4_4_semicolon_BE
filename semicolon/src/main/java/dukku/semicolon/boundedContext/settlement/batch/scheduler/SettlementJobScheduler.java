package dukku.semicolon.boundedContext.settlement.batch.scheduler;

import dukku.semicolon.boundedContext.settlement.batch.config.SettlementBatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 정산 배치 스케줄러
 * - 매월 1일 오전 2시에 정산 배치 Job 실행 (전월 확정 건 대상)
 * - batch.settlement.scheduler.enabled=true 인 경우에만 활성화
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.settlement.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SettlementJobScheduler {

    private final JobOperator jobOperator;
    private final Job settlementJob;
    private final Job settlementRetryJob;
    private final SettlementBatchProperties batchProperties;

    /**
     * 매월 1일 오전 2시에 정산 배치 실행
     * - 전월 구매 확정 건을 대상으로 정산 처리
     * - cron 표현식은 application.yml에서 설정 가능
     * - 기본값: "0 0 2 1 * *" (매월 1일 오전 2시)
     */
    @Scheduled(cron = "${batch.settlement.scheduler.cron:0 0 2 1 * *}")
    public JobExecution runSettlementJob() {
        log.info("========== 정산 배치 스케줄러 시작 ==========");

        try {
            JobParameters jobParameters = createJobParameters();

            log.info("정산 배치 Job 실행 - Parameters: {}", jobParameters);

            JobExecution jobExecution = jobOperator.run(settlementJob, jobParameters);

            log.info("정산 배치 Job 실행 완료 - ExecutionId: {}, Status: {}",
                    jobExecution.getId(), jobExecution.getStatus());
            return jobExecution;
        } catch (Exception e) {
            log.error("정산 배치 실행 중 에러 발생", e);
            throw new RuntimeException("정산 배치 실행 실패", e);
        }
    }
    /**
     * 정산 재처리 배치 실행 (수동 전용)
     * - 실패 건 발생 시 관리자가 수동으로 재처리
     */
    public JobExecution runSettlementRetryJob() {
        log.info("========== 정산 재처리 배치 스케줄러 시작 ==========");

        try {
            JobParameters jobParameters = createJobParameters();

            log.info("정산 재처리 배치 Job 실행 - Parameters: {}", jobParameters);

            JobExecution jobExecution = jobOperator.start(settlementRetryJob, jobParameters);

            log.info("정산 재처리 배치 Job 실행 완료 - ExecutionId: {}, Status: {}",
                    jobExecution.getId(), jobExecution.getStatus());
            return jobExecution;
        } catch (Exception e) {
            log.error("정산 재처리 배치 실행 중 에러 발생", e);
            throw new RuntimeException("정산 재처리 배치 실행 실패", e);
        }
    }

    /**
     * 수동 실행용 메서드 (관리자 API에서 호출 가능)
     */
    public JobExecution runManually() {
        log.info("========== 정산 배치 수동 실행 ==========");
        return runSettlementJob();
    }

    /**
     * 재처리 배치 수동 실행용 메서드 (관리자 API에서 호출 가능)
     */
    public JobExecution runRetryManually() {
        log.info("========== 정산 재처리 배치 수동 실행 ==========");
        return runSettlementRetryJob();
    }

    /**
     * Job 파라미터 생성
     * - timestamp: 실행 시점 (중복 실행 방지)
     * - executionDate: 실행 날짜 (로깅용)
     * - now: 배치 실행 기준 시각 (정산 대상 조회용)
     */
    private JobParameters createJobParameters() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String executionDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return new JobParametersBuilder()
                .addString("timestamp", timestamp)
                .addString("executionDate", executionDate)
                .addLocalDateTime("now", now)
                .toJobParameters();
    }
}
