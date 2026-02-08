package dukku.semicolon.shared.settlement.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배치 Job 실행 통계 응답 DTO
 */
public record SettlementBatchJobStatisticsResponse(
        // 조회 기간 내 배치 현황
        List<JobStatusCount> jobStatus,

        // 최근 실패한 배치 목록
        List<FailedJobInfo> recentFailedJobs,

        // 재시작 가능한 Job 목록
        List<RestartableJobInfo> restartableJobs,

        // 가장 많이 발생하는 에러 TOP 10
        List<ErrorOccurrence> topErrors,

        // 기간 내 정산 처리 건수 (일별)
        List<DailySettlementCount> settlementCounts,

        // 통계 메타 정보
        StatisticsMetadata metadata
) {
    /**
     * 배치 Job 상태별 건수
     */
    public record JobStatusCount(
            String jobName,
            String status,
            long count
    ) {}

    /**
     * 최근 실패한 Job 정보
     */
    public record FailedJobInfo(
            Long jobExecutionId,
            String jobName,
            LocalDateTime startTime,
            String exitMessage
    ) {}

    /**
     * 재시작 가능한 Job 정보
     */
    public record RestartableJobInfo(
            Long jobExecutionId,
            String jobName,
            LocalDateTime startTime,
            String exitMessage
    ) {}

    /**
     * 에러 발생 빈도
     */
    public record ErrorOccurrence(
            String errorMessage,
            long occurrence
    ) {}

    /**
     * 일별 정산 처리 건수
     */
    public record DailySettlementCount(
            String date,
            long totalSettlements
    ) {}

    /**
     * 통계 메타 정보
     */
    public record StatisticsMetadata(
            String startDate,
            String endDate,
            boolean hasData,
            String message
    ) {}
}
