package dukku.semicolon.boundedContext.settlement.out;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import dukku.semicolon.shared.settlement.dto.SettlementSearchCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static dukku.semicolon.boundedContext.settlement.entity.QSettlement.settlement;

@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Settlement> search(SettlementSearchCondition condition, Pageable pageable) {
        List<Settlement> content = queryFactory
                .selectFrom(settlement)
                .where(
                        statusEq(condition.status()),
                        sellerUuidEq(condition.sellerUuid()),
                        createdAtGoe(condition.startDate()),
                        createdAtLt(condition.endDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(settlement.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(settlement.count())
                .from(settlement)
                .where(
                        statusEq(condition.status()),
                        sellerUuidEq(condition.sellerUuid()),
                        createdAtGoe(condition.startDate()),
                        createdAtLt(condition.endDate())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 전체 통계 조회
     * @return Tuple: [count, totalAmount합계, settlementAmount합계, feeAmount합계]
     */
    @Override
    public Tuple getTotalStatistics() {
        return queryFactory
                .select(
                        settlement.count(),
                        settlement.totalAmount.sum().coalesce(0),
                        settlement.settlementAmount.sum().coalesce(0),
                        settlement.feeAmount.sum().coalesce(0)
                )
                .from(settlement)
                .fetchOne();
    }

    /**
     * 상태별 통계 조회 (groupBy)
     * @return List<Tuple>: 각 Tuple은 [status, count, settlementAmount합계]
     */
    @Override
    public List<Tuple> getStatisticsByStatus() {
        return queryFactory
                .select(
                        settlement.settlementStatus,
                        settlement.count(),
                        settlement.settlementAmount.sum().coalesce(0)
                )
                .from(settlement)
                .groupBy(settlement.settlementStatus)
                .fetch();
    }

    @Override
    public long countByCondition(SettlementStatisticsCondition condition) {
        Long count = queryFactory
                .select(settlement.count())
                .from(settlement)
                .where(
                        statusEq(condition.status()),
                        sellerUuidEq(condition.sellerUuid()),
                        completedAtGoe(condition.startDate()),
                        completedAtLt(condition.endDate())
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long sumSettlementAmountByCondition(SettlementStatisticsCondition condition) {
        Long sum = queryFactory
                .select(settlement.settlementAmount.sum().longValue())
                .from(settlement)
                .where(
                        statusEq(condition.status()),
                        sellerUuidEq(condition.sellerUuid()),
                        completedAtGoe(condition.startDate()),
                        completedAtLt(condition.endDate())
                )
                .fetchOne();

        return sum != null ? sum : 0L;
    }

    private BooleanExpression statusEq(SettlementStatus status) {
        return status != null ? settlement.settlementStatus.eq(status) : null;
    }

    private BooleanExpression sellerUuidEq(UUID sellerUuid) {
        return sellerUuid != null ? settlement.sellerUuid.eq(sellerUuid) : null;
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? settlement.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression createdAtLt(LocalDate endDate) {
        return endDate != null ? settlement.createdAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }

    private BooleanExpression completedAtGoe(LocalDate startDate) {
        return startDate != null ? settlement.completedAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression completedAtLt(LocalDate endDate) {
        return endDate != null ? settlement.completedAt.lt(endDate.plusDays(1).atStartOfDay()) : null;
    }
}
