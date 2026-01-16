package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 주문 상품 Repository (스냅샷)
 */
public interface PaymentOrderItemRepository extends JpaRepository<PaymentOrderItem, Integer> {

    Optional<PaymentOrderItem> findByUuid(UUID uuid);

    List<PaymentOrderItem> findByPaymentId(int paymentId);

    List<PaymentOrderItem> findBySellerUuid(UUID sellerUuid);
}
