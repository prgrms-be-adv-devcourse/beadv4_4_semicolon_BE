package dukku.semicolon.boundedContext.settlement.out;

import dukku.semicolon.shared.settlement.dto.SellerStatisticsResponse.SellerSettlementSummary;
import dukku.semicolon.shared.settlement.dto.SettlementFinancialStatisticsResponse;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.MonthlyPendingTrend;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.MonthlyTrend;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.ProcessingTimeStats;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 정산 리포트/통계 전용 Repository (관리자 페이지, Grafana 용)
 * JdbcTemplate 사용 - SQL 직관적, 튜닝 용이
 */
@Repository
@RequiredArgsConstructor
public class SettlementReportRepository {

    private final JdbcTemplate jdbcTemplate;

    // ===== 재무 통계 =====

    public SettlementFinancialStatisticsResponse getFinancialStatistics() {
        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN fee_amount ELSE 0 END), 0) AS platform_revenue,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PENDING' THEN settlement_amount ELSE 0 END), 0) AS pending_amount,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PROCESSING' THEN settlement_amount ELSE 0 END), 0) AS processing_amount,
                    COALESCE(SUM(CASE WHEN settlement_status = 'FAILED' THEN settlement_amount ELSE 0 END), 0) AS failed_amount,
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN total_amount ELSE 0 END), 0) AS total_transaction_amount,
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN settlement_amount ELSE 0 END), 0) AS total_settled_amount,
                    COALESCE(AVG(fee), 0) AS avg_fee_rate
                FROM settlements
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new SettlementFinancialStatisticsResponse(
                        rs.getLong("platform_revenue"),
                        rs.getLong("pending_amount"),
                        rs.getLong("processing_amount"),
                        rs.getLong("failed_amount"),
                        rs.getLong("total_transaction_amount"),
                        rs.getLong("total_settled_amount"),
                        toBigDecimal(rs.getDouble("avg_fee_rate"))
                )
        );
    }

    // ===== 판매자별 통계 =====

    public List<SellerSettlementSummary> getSellerStatistics(int limit, int offset) {
        String sql = """
                SELECT
                    seller_uuid,
                    COUNT(*) AS total_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS success_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failed_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pending_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN settlement_amount ELSE 0 END), 0) AS total_settled,
                    COALESCE(SUM(CASE WHEN settlement_status = 'SUCCESS' THEN fee_amount ELSE 0 END), 0) AS total_fee
                FROM settlements
                GROUP BY seller_uuid
                ORDER BY COUNT(*) DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        SellerSettlementSummary.of(
                                UUID.fromString(rs.getString("seller_uuid")),
                                rs.getLong("total_count"),
                                rs.getLong("success_count"),
                                rs.getLong("failed_count"),
                                rs.getLong("pending_count"),
                                rs.getLong("total_settled"),
                                rs.getLong("total_fee")
                        ),
                limit, offset
        );
    }

    public long countDistinctSellers() {
        String sql = "SELECT COUNT(DISTINCT seller_uuid) FROM settlements";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    // ===== 트렌드 통계 =====

    public List<MonthlyTrend> getMonthlyTrend(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    EXTRACT(YEAR FROM completed_at)::integer AS year,
                    EXTRACT(MONTH FROM completed_at)::integer AS month,
                    COUNT(*) AS settlement_count,
                    COALESCE(SUM(settlement_amount), 0) AS settlement_amount,
                    COALESCE(SUM(fee_amount), 0) AS fee_amount,
                    COALESCE(SUM(total_amount), 0) AS total_amount
                FROM settlements
                WHERE settlement_status = 'SUCCESS'
                    AND completed_at >= ?
                    AND completed_at < ?
                GROUP BY EXTRACT(YEAR FROM completed_at), EXTRACT(MONTH FROM completed_at)
                ORDER BY year DESC, month DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new MonthlyTrend(
                                rs.getInt("year"),
                                rs.getInt("month"),
                                rs.getLong("settlement_count"),
                                rs.getLong("settlement_amount"),
                                rs.getLong("fee_amount"),
                                rs.getLong("total_amount")
                        ),
                startDate, endDate
        );
    }

    /**
     * 월별 정산 예정 금액 (PENDING/PROCESSING 상태)
     * - 정산 예약일 기준으로 월별 집계
     */
    public List<MonthlyPendingTrend> getMonthlyPendingTrend() {
        String sql = """
                SELECT
                    EXTRACT(YEAR FROM settlement_reservation_date)::integer AS year,
                    EXTRACT(MONTH FROM settlement_reservation_date)::integer AS month,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pending_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PENDING' THEN settlement_amount ELSE 0 END), 0) AS pending_amount,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PROCESSING' THEN 1 ELSE 0 END), 0) AS processing_count,
                    COALESCE(SUM(CASE WHEN settlement_status = 'PROCESSING' THEN settlement_amount ELSE 0 END), 0) AS processing_amount
                FROM settlements
                WHERE settlement_status IN ('PENDING', 'PROCESSING')
                GROUP BY EXTRACT(YEAR FROM settlement_reservation_date), EXTRACT(MONTH FROM settlement_reservation_date)
                ORDER BY year DESC, month DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MonthlyPendingTrend(
                        rs.getInt("year"),
                        rs.getInt("month"),
                        rs.getLong("pending_count"),
                        rs.getLong("pending_amount"),
                        rs.getLong("processing_count"),
                        rs.getLong("processing_amount")
                )
        );
    }

    public ProcessingTimeStats getProcessingTimeStats() {
        String sql = """
                SELECT
                    COALESCE(AVG(EXTRACT(EPOCH FROM (completed_at - settlement_reservation_date)) / 3600.0), 0) AS avg_reservation_hours,
                    COALESCE(AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600.0), 0) AS avg_creation_hours,
                    COALESCE(MIN(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600.0), 0) AS min_hours,
                    COALESCE(MAX(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600.0), 0) AS max_hours
                FROM settlements
                WHERE settlement_status = 'SUCCESS'
                    AND completed_at IS NOT NULL
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new ProcessingTimeStats(
                        round(rs.getDouble("avg_reservation_hours")),
                        round(rs.getDouble("avg_creation_hours")),
                        round(rs.getDouble("min_hours")),
                        round(rs.getDouble("max_hours"))
                )
        );
    }

    // ===== 헬퍼 =====

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
