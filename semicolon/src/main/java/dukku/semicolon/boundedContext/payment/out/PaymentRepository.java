package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.common.shared.payment.type.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 Repository
 */
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByUuid(UUID uuid);

    List<Payment> findByUserUuid(UUID userUuid);

    List<Payment> findByOrderUuid(UUID orderUuid);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    Optional<Payment> findByPgPaymentKey(String pgPaymentKey);

    Optional<Payment> findByTossOrderId(String tossOrderId);
}
