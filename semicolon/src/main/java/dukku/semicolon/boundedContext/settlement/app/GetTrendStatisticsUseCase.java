package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.boundedContext.settlement.out.SettlementReportRepository;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.MonthlyPendingTrend;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.MonthlyTrend;
import dukku.semicolon.shared.settlement.dto.SettlementTrendStatisticsResponse.ProcessingTimeStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 트렌드 통계 조회 UseCase
 */
@Component
@RequiredArgsConstructor
public class GetTrendStatisticsUseCase {

    private final SettlementReportRepository reportRepository;

    @Transactional(readOnly = true)
    public SettlementTrendStatisticsResponse execute(LocalDate startDate, LocalDate endDate) {
        LocalDate adjustedEndDate = endDate.plusDays(1);

        List<MonthlyTrend> monthlyTrends = reportRepository.getMonthlyTrend(startDate, adjustedEndDate);
        List<MonthlyPendingTrend> monthlyPendingTrends = reportRepository.getMonthlyPendingTrend();
        ProcessingTimeStats processingTimeStats = reportRepository.getProcessingTimeStats();

        return new SettlementTrendStatisticsResponse(monthlyTrends, monthlyPendingTrends, processingTimeStats);
    }
}
