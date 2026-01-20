package dukku.semicolon.boundedContext.settlement.out;

import com.querydsl.core.Tuple;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.dto.SettlementSearchCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementRepositoryCustom {

    Page<Settlement> search(SettlementSearchCondition condition, Pageable pageable);

    /**
     * 전체 통계 조회
     * @return Tuple: [count, totalAmount합계, settlementAmount합계, feeAmount합계]
     */
    Tuple getTotalStatistics();

    /**
     * 상태별 통계 조회 (groupBy)
     * @return List<Tuple>: 각 Tuple은 [status, count, settlementAmount합계]
     */
    List<Tuple> getStatisticsByStatus();

    /**
     * 동적 조건에 따른 정산 건수 조회
     */
    long countByCondition(SettlementStatisticsCondition condition);

    /**
     * 동적 조건에 따른 정산 금액 합계 조회
     */
    long sumSettlementAmountByCondition(SettlementStatisticsCondition condition);
}
