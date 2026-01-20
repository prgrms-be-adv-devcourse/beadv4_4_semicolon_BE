package dukku.semicolon.boundedContext.order.out;

import dukku.semicolon.boundedContext.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Optional<OrderItem> findByUuid(UUID orderItemUuid);
}
