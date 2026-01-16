package dukku.semicolon.boundedContext.payment.out;

import dukku.semicolon.boundedContext.payment.entity.Refund;
import dukku.semicolon.boundedContext.payment.entity.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 환불 Repository
 */
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    Optional<Refund> findByUuid(UUID uuid);

    List<Refund> findByPaymentId(int paymentId);

    List<Refund> findByRefundStatus(RefundStatus status);
}
