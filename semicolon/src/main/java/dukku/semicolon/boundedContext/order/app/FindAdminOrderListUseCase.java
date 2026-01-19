package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.ForbiddenException;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.out.OrderRepository;
import dukku.semicolon.shared.order.dto.AdminOrderSearchCondition;
import dukku.semicolon.shared.order.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindAdminOrderListUseCase {
    private final OrderRepository orderRepository;

    public Page<OrderListResponse> execute(AdminOrderSearchCondition condition, Pageable pageable) {
        if (!UserUtil.isAdmin()) {
            throw new ForbiddenException("관리자만 조회할 수 있습니다.");
        }

        Page<Order> orders = orderRepository.searchForAdmin(condition, pageable);

        return orders.map(OrderListResponse::from);
    }
}