package dukku.semicolon.boundedContext.settlement.app;

import com.querydsl.core.Tuple;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static dukku.semicolon.boundedContext.settlement.entity.QSettlement.settlement;

@Component
@RequiredArgsConstructor
public class GetSettlementStatisticsUseCase {

    private final SettlementRepository settlementRepository;

    /**
     * 정산 통계 조회 (동적 조건)
     * 전체 통계 + 상태별 통계 + 기간 내 완료
     */
    @Transactional(readOnly = true)
    public SettlementStatisticsResponse execute(SettlementStatisticsCondition condition) {
        TotalStatistics total = toTotalStatistics(settlementRepository.getTotalStatistics());

        StatusStatisticsMap statusStats = StatusStatisticsMap.from(
                settlementRepository.getStatisticsByStatus()
        );

        CompletedInPeriod completed = getCompletedInPeriod(condition);

        return new SettlementStatisticsResponse(
                total.totalCount(),
                total.totalAmount(),
                total.totalSettlementAmount(),
                total.totalFeeAmount(),

                statusStats.count(SettlementStatus.CREATED),
                statusStats.count(SettlementStatus.PROCESSING),
                statusStats.count(SettlementStatus.PENDING),
                statusStats.count(SettlementStatus.SUCCESS),
                statusStats.count(SettlementStatus.FAILED),

                statusStats.amount(SettlementStatus.CREATED),
                statusStats.amount(SettlementStatus.PROCESSING),
                statusStats.amount(SettlementStatus.PENDING),
                statusStats.amount(SettlementStatus.SUCCESS),
                statusStats.amount(SettlementStatus.FAILED),

                completed.count(),
                completed.amount()
        );
    }

    private TotalStatistics toTotalStatistics(Tuple tuple) {
        return new TotalStatistics(
                tuple.get(settlement.count()),
                tuple.get(settlement.totalAmount.sum().coalesce(0L)),
                tuple.get(settlement.settlementAmount.sum().coalesce(0L)),
                tuple.get(settlement.feeAmount.sum().coalesce(0L))
        );
    }

    private CompletedInPeriod getCompletedInPeriod(SettlementStatisticsCondition condition) {
        SettlementStatisticsCondition completedCondition = new SettlementStatisticsCondition(
                SettlementStatus.SUCCESS,
                condition.sellerUuid(),
                condition.startDate(),
                condition.endDate()
        );

        return new CompletedInPeriod(
                settlementRepository.countByCondition(completedCondition),
                settlementRepository.sumSettlementAmountByCondition(completedCondition)
        );
    }

    /**
     * 전체 통계
     */
    private record TotalStatistics(long totalCount, long totalAmount, long totalSettlementAmount, long totalFeeAmount) {
    }

    /**
     * 기간 내 완료된 정산 통계
     */
    private record CompletedInPeriod(long count, long amount) {
    }

    /**
     * 상태별 통계 단건
     */
    private record StatusStatistics(SettlementStatus status, long count, long settlementAmount) {
        static final StatusStatistics EMPTY = new StatusStatistics(null, 0L, 0L);
    }

    /**
     * 상태별 통계 일급 컬렉션
     */
    private static class StatusStatisticsMap {

        private final Map<SettlementStatus, StatusStatistics> map;

        private StatusStatisticsMap(Map<SettlementStatus, StatusStatistics> map) {
            this.map = map;
        }

        static StatusStatisticsMap from(List<Tuple> tuples) {
            Map<SettlementStatus, StatusStatistics> map = new EnumMap<>(SettlementStatus.class);
            for (Tuple tuple : tuples) {
                SettlementStatus status = tuple.get(settlement.settlementStatus);
                map.put(status, new StatusStatistics(
                        status,
                        tuple.get(settlement.count()),
                        tuple.get(settlement.settlementAmount.sum().coalesce(0L))
                ));
            }
            return new StatusStatisticsMap(map);
        }

        long count(SettlementStatus status) {
            return map.getOrDefault(status, StatusStatistics.EMPTY).count();
        }

        long amount(SettlementStatus status) {
            return map.getOrDefault(status, StatusStatistics.EMPTY).settlementAmount();
        }
    }
}
