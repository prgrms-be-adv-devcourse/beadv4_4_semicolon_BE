package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentHistoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 이력 Repository
 */
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Integer> {

    Optional<PaymentHistory> findByUuid(UUID uuid);

    List<PaymentHistory> findByPaymentId(int paymentId);

    List<PaymentHistory> findByType(PaymentHistoryType type);
}
