package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.out.OrderRepository;
import dukku.semicolon.shared.order.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMyOrderListUseCase {
    private final OrderRepository orderRepository;

    public Page<OrderListResponse> execute(Pageable pageable) {
        UUID currentUserId = UserUtil.getUserId();

        Page<Order> orders = orderRepository.findAllMyOrders(currentUserId, pageable);

        return orders.map(OrderListResponse::from);
    }
}