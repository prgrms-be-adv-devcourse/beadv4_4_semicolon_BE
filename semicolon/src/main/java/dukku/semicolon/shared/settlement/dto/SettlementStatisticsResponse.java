package dukku.semicolon.shared.settlement.dto;

/**
 * 정산 통계 응답 DTO
 * GET /admin/settlements/statistics
 *
 */
public record SettlementStatisticsResponse(
        // 전체 통계
        long totalCount,
        long totalAmount,
        long totalSettlementAmount,
        long totalFeeAmount,

        // 상태별 건수
        long createdCount,
        long processingCount,
        long pendingCount,
        long successCount,
        long failedCount,

        // 상태별 정산 금액
        long createdAmount,
        long processingAmount,
        long pendingAmount,
        long successAmount,
        long failedAmount,

        // 조회 기간 내 완료된 정산
        long completedCountInPeriod,
        long completedAmountInPeriod
) {
}
