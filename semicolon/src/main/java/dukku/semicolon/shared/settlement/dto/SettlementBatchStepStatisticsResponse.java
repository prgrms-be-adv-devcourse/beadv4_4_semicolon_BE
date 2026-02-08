package dukku.semicolon.shared.settlement.dto;

import java.util.List;

/**
 * 배치 Step 실행 통계 응답 DTO
 */
public record SettlementBatchStepStatisticsResponse(
        List<StepPerformance> stepPerformances,
        StatisticsMetadata metadata
) {
    /**
     * Step별 성능 통계
     */
    public record StepPerformance(
            String stepName,
            double avgDurationSeconds,
            double avgReadCount,
            double avgWriteCount,
            double avgSkipCount,
            long totalExecutions
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
