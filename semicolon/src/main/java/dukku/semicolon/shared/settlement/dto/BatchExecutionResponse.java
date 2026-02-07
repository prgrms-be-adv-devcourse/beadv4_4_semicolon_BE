package dukku.semicolon.shared.settlement.dto;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;

import java.time.LocalDateTime;

/**
 * 배치 실행 결과 응답 DTO
 */
public record BatchExecutionResponse(
        Long jobExecutionId,
        String jobName,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String exitCode,
        String exitDescription
) {
    public static BatchExecutionResponse from(JobExecution jobExecution) {
        return new BatchExecutionResponse(
                jobExecution.getId(),
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus().name(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getExitStatus().getExitCode(),
                jobExecution.getExitStatus().getExitDescription()
        );
    }

    public static BatchExecutionResponse started(Long jobExecutionId, String jobName) {
        return new BatchExecutionResponse(
                jobExecutionId,
                jobName,
                BatchStatus.STARTED.name(),
                LocalDateTime.now(),
                null,
                "EXECUTING",
                "배치 작업이 시작되었습니다."
        );
    }

    /**
     * 배치 시작 응답 생성 (JobExecution 없이)
     * - Scheduler가 void를 반환하므로 단순 응답 생성
     */
    public static BatchExecutionResponse started(String jobName) {
        return new BatchExecutionResponse(
                null,
                jobName,
                "STARTED",
                LocalDateTime.now(),
                null,
                "EXECUTING",
                "배치 작업이 시작되었습니다. 실행 결과는 로그 또는 배치 통계 API에서 확인하세요."
        );
    }
}
