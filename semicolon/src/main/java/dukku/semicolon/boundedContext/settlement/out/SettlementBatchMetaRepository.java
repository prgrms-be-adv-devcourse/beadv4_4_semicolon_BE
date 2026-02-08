package dukku.semicolon.boundedContext.settlement.out;

import dukku.semicolon.shared.settlement.dto.SettlementBatchJobStatisticsResponse.*;
import dukku.semicolon.shared.settlement.dto.SettlementBatchStepStatisticsResponse.StepPerformance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 배치 메타테이블 조회 Repository
 * Spring Batch 메타테이블(BATCH_JOB_EXECUTION, BATCH_JOB_INSTANCE, BATCH_STEP_EXECUTION)을 조회
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SettlementBatchMetaRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Spring Batch 메타 테이블 존재 여부 확인
     */
    public boolean isBatchMetaTablesExist() {
        try {
            String sql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_name IN ('batch_job_execution', 'batch_job_instance', 'batch_step_execution')
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null && count == 3;
        } catch (DataAccessException e) {
            log.error("메타 테이블 존재 여부 확인 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 기간 내 배치 실행 현황 조회 (Job별, 상태별)
     */
    public List<JobStatusCount> findJobStatusByDateRange(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT jin.job_name, jex.status, COUNT(*) AS count
                FROM batch_job_execution jex
                JOIN batch_job_instance jin ON jex.job_instance_id = jin.job_instance_id
                WHERE DATE(jex.start_time) BETWEEN ? AND ?
                GROUP BY jin.job_name, jex.status
                ORDER BY jin.job_name, jex.status
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new JobStatusCount(
                    rs.getString("job_name"),
                    rs.getString("status"),
                    rs.getLong("count")
            ), startDate, endDate);
        }


    /**
     * 기간 내 실패한 배치 조회
     */
    public List<FailedJobInfo> findFailedJobs(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT jex.job_execution_id, jin.job_name, jex.start_time, jex.exit_message
                FROM batch_job_execution jex
                JOIN batch_job_instance jin ON jex.job_instance_id = jin.job_instance_id
                WHERE jex.status = 'FAILED'
                AND DATE(jex.start_time) BETWEEN ? AND ?
                ORDER BY jex.start_time DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new FailedJobInfo(
                    rs.getLong("job_execution_id"),
                    rs.getString("job_name"),
                    rs.getTimestamp("start_time") != null
                            ? rs.getTimestamp("start_time").toLocalDateTime()
                            : null,
                    rs.getString("exit_message")
            ), startDate, endDate);
        }

    /**
     * 재시작 가능한 Job 조회 (FAILED 상태이면서 동일 인스턴스에 COMPLETED가 없는 것)
     */
    public List<RestartableJobInfo> findRestartableJobs(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT jex.job_execution_id, jin.job_name, jex.start_time, jex.exit_message
                FROM batch_job_execution jex
                JOIN batch_job_instance jin ON jex.job_instance_id = jin.job_instance_id
                WHERE jex.status = 'FAILED'
                AND DATE(jex.start_time) BETWEEN ? AND ?
                AND NOT EXISTS (
                    SELECT 1 FROM batch_job_execution jex2
                    WHERE jex2.job_instance_id = jin.job_instance_id
                    AND jex2.status = 'COMPLETED'
                )
                ORDER BY jex.start_time DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new RestartableJobInfo(
                    rs.getLong("job_execution_id"),
                    rs.getString("job_name"),
                    rs.getTimestamp("start_time") != null
                            ? rs.getTimestamp("start_time").toLocalDateTime()
                            : null,
                    rs.getString("exit_message")
            ), startDate, endDate);
        }

    /**
     * 기간 내 가장 많이 발생하는 에러 TOP 10
     */
    public List<ErrorOccurrence> findTopErrors(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT SUBSTRING(exit_message, 1, 200) AS error_msg, COUNT(*) AS occurrence
                FROM batch_job_execution
                WHERE status = 'FAILED'
                AND DATE(start_time) BETWEEN ? AND ?
                AND exit_message IS NOT NULL
                AND exit_message != ''
                GROUP BY SUBSTRING(exit_message, 1, 200)
                ORDER BY occurrence DESC
                LIMIT 10
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new ErrorOccurrence(
                    rs.getString("error_msg"),
                    rs.getLong("occurrence")
            ), startDate, endDate);
        }

    /**
     * 기간 내 정산 처리 건수 (일별)
     */
    public List<DailySettlementCount> findSettlementCounts(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT DATE(jex.start_time) AS date, SUM(sex.write_count) AS total_settlements
                FROM batch_step_execution sex
                JOIN batch_job_execution jex ON sex.job_execution_id = jex.job_execution_id
                JOIN batch_job_instance jin ON jex.job_instance_id = jin.job_instance_id
                WHERE jin.job_name = 'settlementJob'
                AND jex.status = 'COMPLETED'
                AND DATE(jex.start_time) BETWEEN ? AND ?
                GROUP BY DATE(jex.start_time)
                ORDER BY date DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new DailySettlementCount(
                    rs.getString("date"),
                    rs.getLong("total_settlements")
            ), startDate, endDate);
        }

    /**
     * 기간 내 Step별 평균 처리 시간 및 성능 통계
     */
    public List<StepPerformance> findStepPerformances(LocalDate startDate, LocalDate endDate) {
            String sql = """
                SELECT
                    step_name,
                    COALESCE(AVG(EXTRACT(EPOCH FROM (end_time - start_time))), 0) AS avg_sec,
                    COALESCE(AVG(read_count), 0) AS avg_read,
                    COALESCE(AVG(write_count), 0) AS avg_write,
                    COALESCE(AVG(read_skip_count + process_skip_count + write_skip_count), 0) AS avg_skip,
                    COUNT(*) AS total_executions
                FROM batch_step_execution
                WHERE status = 'COMPLETED'
                AND DATE(start_time) BETWEEN ? AND ?
                AND start_time IS NOT NULL
                AND end_time IS NOT NULL
                GROUP BY step_name
                ORDER BY step_name
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> new StepPerformance(
                    rs.getString("step_name"),
                    rs.getDouble("avg_sec"),
                    rs.getDouble("avg_read"),
                    rs.getDouble("avg_write"),
                    rs.getDouble("avg_skip"),
                    rs.getLong("total_executions")
            ), startDate, endDate);
        }
}
