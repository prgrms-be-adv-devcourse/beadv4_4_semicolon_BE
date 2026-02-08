package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.boundedContext.settlement.out.SettlementBatchMetaRepository;
import dukku.semicolon.shared.settlement.dto.SettlementBatchJobStatisticsResponse;
import dukku.semicolon.shared.settlement.dto.SettlementBatchJobStatisticsResponse.*;
import dukku.semicolon.shared.settlement.dto.SettlementBatchStepStatisticsResponse;
import dukku.semicolon.shared.settlement.dto.SettlementBatchStepStatisticsResponse.StepPerformance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * 배치 통계 조회 UseCase
 * Spring Batch 메타테이블 기반 통계
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetBatchStatisticsUseCase {

    private final SettlementBatchMetaRepository batchMetaRepository;

    /**
     * 배치 Job 통계 조회
     */
    @Transactional(readOnly = true)
    public SettlementBatchJobStatisticsResponse getJobStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("배치 Job 통계 조회 - 기간: {} ~ {}", startDate, endDate);

        // 메타테이블 존재 여부 확인
        if (!batchMetaRepository.isBatchMetaTablesExist()) {
            log.warn("Spring Batch 메타 테이블이 존재하지 않습니다.");
            return createEmptyJobStatistics(startDate, endDate, "Spring Batch 메타 테이블이 존재하지 않습니다. 배치를 한 번 이상 실행해주세요.");
        }

        List<JobStatusCount> jobStatus = batchMetaRepository.findJobStatusByDateRange(startDate, endDate);
        List<FailedJobInfo> failedJobs = batchMetaRepository.findFailedJobs(startDate, endDate);
        List<RestartableJobInfo> restartableJobs = batchMetaRepository.findRestartableJobs(startDate, endDate);
        List<ErrorOccurrence> topErrors = batchMetaRepository.findTopErrors(startDate, endDate);
        List<DailySettlementCount> settlementCounts = batchMetaRepository.findSettlementCounts(startDate, endDate);

        // 데이터가 전혀 없는 경우
        if (jobStatus.isEmpty() && failedJobs.isEmpty() && settlementCounts.isEmpty()) {
            log.info("기간 내 배치 실행 이력이 없습니다: {} ~ {}", startDate, endDate);
            return createEmptyJobStatistics(startDate, endDate, "조회 기간 내 배치 실행 이력이 없습니다.");
        }

        StatisticsMetadata metadata = new StatisticsMetadata(startDate.toString(), endDate.toString(), true, "정상 조회됨");

        return new SettlementBatchJobStatisticsResponse(
                jobStatus,
                failedJobs,
                restartableJobs,
                topErrors,
                settlementCounts,
                metadata
            );
        }


    /**
     * 빈 Job 통계 응답 생성
     */
    private SettlementBatchJobStatisticsResponse createEmptyJobStatistics(
            LocalDate startDate, LocalDate endDate, String message) {
        StatisticsMetadata metadata = new StatisticsMetadata(
            startDate.toString(),
            endDate.toString(),
            false,
            message
        );

        return new SettlementBatchJobStatisticsResponse(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                metadata
        );
    }


    /**
     * 배치 Step 통계 조회
     */
    @Transactional(readOnly = true)
    public SettlementBatchStepStatisticsResponse getStepStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("배치 Step 통계 조회 - 기간: {} ~ {}", startDate, endDate);

        // 메타테이블 존재 여부 확인
        if (!batchMetaRepository.isBatchMetaTablesExist()) {
            log.warn("Spring Batch 메타 테이블이 존재하지 않습니다.");
            return createEmptyStepStatistics(startDate, endDate, "Spring Batch 메타 테이블이 존재하지 않습니다. 배치를 한 번 이상 실행해주세요.");}

        List<StepPerformance> stepPerformances = batchMetaRepository.findStepPerformances(startDate, endDate);

        // 데이터가 없는 경우
        if (stepPerformances.isEmpty()) {
            log.info("기간 내 Step 실행 이력이 없습니다: {} ~ {}", startDate, endDate);
            return createEmptyStepStatistics(startDate, endDate, "조회 기간 내 Step 실행 이력이 없습니다.");}

        SettlementBatchStepStatisticsResponse.StatisticsMetadata metadata = new SettlementBatchStepStatisticsResponse.StatisticsMetadata(
                startDate.toString(),
                endDate.toString(),
                true,
                "정상 조회됨"
        );

        return new SettlementBatchStepStatisticsResponse(stepPerformances, metadata);
    }


    /**
     * 빈 Step 통계 응답 생성
     */
    private SettlementBatchStepStatisticsResponse createEmptyStepStatistics(
            LocalDate startDate, LocalDate endDate, String message) {
        SettlementBatchStepStatisticsResponse.StatisticsMetadata metadata =
                new SettlementBatchStepStatisticsResponse.StatisticsMetadata(
                        startDate.toString(),
                        endDate.toString(),
                        false,
                        message
                );

        return new SettlementBatchStepStatisticsResponse(List.of(), metadata);
    }
}

