package dukku.semicolon.boundedContext.settlement.in.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 정산 배치 작업 리스너
 * - Job/Step 시작/종료 로깅
 * - Skip 발생 시 로깅
 */
@Slf4j
@Component
public class SettlementBatchListener implements JobExecutionListener, StepExecutionListener {


    @Override
    public void beforeJob(JobExecution jobExecution) {
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

        // 실패한 경우 예외 정보 출력
        if (jobExecution.getAllFailureExceptions().size() > 0) {
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
}
