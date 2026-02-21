package dukku.settlement.boundedContext.settlement.batch.listener;

import dukku.settlement.boundedContext.settlement.app.SettlementMetrics;
import dukku.settlement.boundedContext.settlement.batch.notification.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 정산 배치 작업 리스너
 * - Job/Step 시작/종료 로깅
 * - Skip 발생 시 로깅
 * - Job 완료 시 Slack 알림 전송
 * - Job 실행 시간 / Skip / Retry 메트릭 기록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchListener implements JobExecutionListener, StepExecutionListener {

    private final SlackNotificationService slackNotificationService;
    private final SettlementMetrics settlementMetrics;


    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put("traceId", "batch-" + jobExecution.getId());
        MDC.put("jobName", jobExecution.getJobInstance().getJobName());
        log.info("========== 정산 배치 작업 시작 ==========");
        log.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Parameters: {}", jobExecution.getJobParameters());
        log.info("Start Time: {}", jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("========== 정산 배치 작업 종료 ==========");
        log.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("Status: {}", jobExecution.getStatus());
        log.info("End Time: {}", jobExecution.getEndTime());

        // 메트릭 기록: 배치 처리 시간 + Skip 건수
        recordJobMetrics(jobExecution);

        // 실패한 경우 예외 정보 출력
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            log.error("실패한 예외 목록:");
            for (Throwable exception : jobExecution.getAllFailureExceptions()) {
                log.error("  - {}: {}", exception.getClass().getSimpleName(), exception.getMessage());
            }
        }

        // Step별 통계 출력
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            log.info("Step [{}] 통계:", stepExecution.getStepName());
            log.info("  - Read Count: {}", stepExecution.getReadCount());
            log.info("  - Write Count: {}", stepExecution.getWriteCount());
            log.info("  - Skip Count: {}", stepExecution.getSkipCount());
            log.info("  - Commit Count: {}", stepExecution.getCommitCount());
            log.info("  - Rollback Count: {}", stepExecution.getRollbackCount());
        }

        // Slack 알림 전송
        slackNotificationService.sendJobCompletionNotification(jobExecution);
        MDC.clear();
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step [{}] 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step [{}] 종료 - Status: {}, Read: {}, Write: {}, Skip: {}",
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());
        return stepExecution.getExitStatus();
    }

    /**
     * Job 종료 후 메트릭 기록
     * - 실행 시간 (Gauge)
     * - 전체 Step의 Skip 건수 합산
     */
    private void recordJobMetrics(JobExecution jobExecution) {
        // 1. 실행 시간 기록
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        if (startTime != null && endTime != null) {
            long durationSeconds = Duration.between(startTime, endTime).toSeconds();
            settlementMetrics.recordJobDuration(durationSeconds);
            log.info("[메트릭] 배치 처리 시간 기록: {}초", durationSeconds);
        }

        // 2. Skip 건수 기록
        long totalSkipCount = jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getSkipCount)
                .sum();

        if (totalSkipCount > 0) {
            settlementMetrics.incrementSkip(totalSkipCount);
            log.info("[메트릭] Skip 건수 기록: {}건", totalSkipCount);
        }
    }
}
