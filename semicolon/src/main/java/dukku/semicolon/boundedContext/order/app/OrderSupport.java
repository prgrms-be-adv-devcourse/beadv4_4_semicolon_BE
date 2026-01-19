package dukku.semicolon.boundedContext.order.app;

import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.out.OrderRepository;
import dukku.semicolon.shared.order.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderSupport {
    private final OrderRepository orderRepository;

    public Order findOrderByUuid(UUID orderUuid) {
        return orderRepository.findByUuid(orderUuid)
                .orElseThrow(OrderNotFoundException::new);
    }

    public Order findOrderByUuidWithItems(UUID orderUuid) {
        return orderRepository.findByUuidWithItems(orderUuid)
                .orElseThrow(OrderNotFoundException::new);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
