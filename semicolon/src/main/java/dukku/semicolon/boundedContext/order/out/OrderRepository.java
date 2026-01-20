package dukku.semicolon.boundedContext.order.out;

import dukku.semicolon.boundedContext.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Integer>, CustomOrderRepository {
    Optional<Order> findByUuid(UUID orderUuid);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.uuid = :uuid")
    Optional<Order> findByUuidWithItems(@Param("uuid") UUID uuid);
}
