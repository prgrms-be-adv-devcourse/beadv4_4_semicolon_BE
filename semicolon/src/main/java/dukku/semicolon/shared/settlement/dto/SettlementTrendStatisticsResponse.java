package dukku.semicolon.shared.settlement.dto;

import java.util.List;

/**
 * 트렌드 통계 응답 DTO
 * - 월 1회 정산 기준으로 월별 통계 중심
 */
public record SettlementTrendStatisticsResponse(
        // 월별 정산 완료 추이
        List<MonthlyTrend> monthlyTrends,

        // 월별 정산 예정 금액 (PENDING 상태 기준)
        List<MonthlyPendingTrend> monthlyPendingTrends,

        // 처리 시간 분석
        ProcessingTimeStats processingTimeStats
) {
    /**
     * 월별 정산 완료 트렌드
     */
    public record MonthlyTrend(
            int year,
            int month,
            long settlementCount,
            long settlementAmount,
            long feeAmount,
            long totalAmount
    ) {}

    /**
     * 월별 정산 예정 트렌드 (PENDING/PROCESSING 상태)
     */
    public record MonthlyPendingTrend(
            int year,
            int month,
            long pendingCount,
            long pendingAmount,
            long processingCount,
            long processingAmount
    ) {}

    /**
     * 처리 시간 통계
     */
    public record ProcessingTimeStats(
            // 평균 처리 시간 (예약일 → 완료일, 시간 단위)
            double avgReservationToCompletionHours,
            // 평균 처리 시간 (생성일 → 완료일, 시간 단위)
            double avgCreationToCompletionHours,
            // 최소 처리 시간
            double minProcessingHours,
            // 최대 처리 시간
            double maxProcessingHours
    ) {}
}
