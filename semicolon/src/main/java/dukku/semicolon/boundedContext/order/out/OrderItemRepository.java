package dukku.semicolon.boundedContext.order.out;

import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    Optional<OrderItem> findByUuid(UUID orderItemUuid);

    List<OrderItem> findAllByStatusAndDeliveryDateBefore(OrderItemStatus status, LocalDateTime dateTime);

    List<OrderItem> findAllByStatusAndConfirmedAtBetween(
            OrderItemStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order WHERE oi.sellerUuid = :sellerUuid")
    Page<OrderItem> findAllBySellerUuidWithOrder(@Param("sellerUuid") UUID sellerUuid, Pageable pageable);
}
