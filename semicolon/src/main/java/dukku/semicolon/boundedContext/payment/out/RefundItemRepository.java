package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 환불 상품 Repository
 */
public interface RefundItemRepository extends JpaRepository<RefundItem, Integer> {

    Optional<RefundItem> findByUuid(UUID uuid);

    List<RefundItem> findByRefundId(int refundId);
}
