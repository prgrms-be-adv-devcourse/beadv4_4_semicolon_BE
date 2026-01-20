package dukku.semicolon.boundedContext.order.out.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dukku.common.shared.order.type.OrderStatus;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.out.CustomOrderRepository;
import dukku.semicolon.shared.order.dto.AdminOrderSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dukku.semicolon.boundedContext.order.entity.QOrder.order;

@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findAllMyOrders(UUID userUuid, Pageable pageable) {
        List<Order> content = queryFactory
                .selectFrom(order)
                .where(
                        order.userUuid.eq(userUuid) // 내 주문만
                )
                .orderBy(order.createdAt.desc()) // ★ 요청된 정렬 무시하고 최신순 고정
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.userUuid.eq(userUuid)
                );

        return PageableExecutionUtils.getPage(content, pageable,
                () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L));
    }

    @Override
    public Page<Order> searchForAdmin(AdminOrderSearchCondition condition, Pageable pageable) {
        List<Order> content = queryFactory
                .selectFrom(order)
                .where(
                        userUuidEq(condition.userUuid()),
                        orderUuidContains(condition.orderUuid()),
                        dateBetween(condition.startDate(), condition.endDate()),
                        statusEq(condition.status())
                )
                .orderBy(order.createdAt.desc()) // 기본은 최신순 (필요 시 pageable.getSort() 연동 가능)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(
                        userUuidEq(condition.userUuid()),
                        orderUuidContains(condition.orderUuid()),
                        dateBetween(condition.startDate(), condition.endDate()),
                        statusEq(condition.status())
                );

        return PageableExecutionUtils.getPage(content, pageable,
                () -> Optional.ofNullable(countQuery.fetchOne()).orElse(0L));
    }

    private BooleanExpression dateBetween(LocalDateTime start, LocalDateTime end) {
        return (start != null && end != null) ? order.createdAt.between(start, end) : null;
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }

    private BooleanExpression userUuidEq(UUID userUuid) {
        return userUuid != null ? order.userUuid.eq(userUuid) : null;
    }

    private BooleanExpression orderUuidContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        try {
            // String -> UUID 변환 후 동등 비교 (eq)
            return order.uuid.eq(UUID.fromString(keyword));
        } catch (IllegalArgumentException e) {
            // 키워드가 UUID 형식이 아니면(예: "abc") 검색 결과 없음 처리
            return null;
        }
    }
}