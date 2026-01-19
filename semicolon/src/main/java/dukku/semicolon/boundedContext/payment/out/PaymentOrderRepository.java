package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 결제 주문 Repository (Order BC 레플리카)
 */
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Integer> {

    Optional<PaymentOrder> findByUuid(UUID uuid);

    Optional<PaymentOrder> findByUserUuid(UUID userUuid);
}
