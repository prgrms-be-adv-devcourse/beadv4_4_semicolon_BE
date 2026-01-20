package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrder;
import dukku.semicolon.boundedContext.payment.entity.Refund;
import dukku.semicolon.boundedContext.payment.out.PaymentHistoryRepository;
import dukku.semicolon.boundedContext.payment.out.PaymentOrderRepository;
import dukku.semicolon.boundedContext.payment.out.PaymentRepository;
import dukku.semicolon.boundedContext.payment.out.RefundRepository;
import dukku.semicolon.shared.payment.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment 도메인 공통 지원 클래스
 *
 * <p>
 * Repository 래핑 및 공통 조회/저장 로직 제공
 * UseCase에서 직접 Repository를 사용하지 않고 이 클래스를 통해 접근
 */
@Component
@RequiredArgsConstructor
public class PaymentSupport {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final RefundRepository refundRepository;

    // === Payment 관련 ===

    /**
     * UUID로 결제 조회 (없으면 예외)
     */
    public Payment findPaymentByUuid(UUID uuid) {
        return paymentRepository.findByUuid(uuid)
                .orElseThrow(PaymentNotFoundException::new);
    }

    /**
     * UUID로 결제 조회 (Optional)
     */
    public Optional<Payment> findPaymentByUuidOptional(UUID uuid) {
        return paymentRepository.findByUuid(uuid);
    }

    /**
     * 사용자 UUID로 결제 목록 조회
     */
    public List<Payment> findPaymentsByUserUuid(UUID userUuid) {
        return paymentRepository.findByUserUuid(userUuid);
    }

    /**
     * PG 결제키로 결제 조회
     */
    public Optional<Payment> findPaymentByPgPaymentKey(String pgPaymentKey) {
        return paymentRepository.findByPgPaymentKey(pgPaymentKey);
    }

    /**
     * 결제 저장
     */
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    // === PaymentOrder 관련 ===

    /**
     * UUID로 결제 주문 조회 (Optional)
     */
    public Optional<PaymentOrder> findPaymentOrderByUuid(UUID uuid) {
        return paymentOrderRepository.findByUuid(uuid);
    }

    /**
     * 결제 주문 저장
     */
    public PaymentOrder savePaymentOrder(PaymentOrder paymentOrder) {
        return paymentOrderRepository.save(paymentOrder);
    }

    // === PaymentHistory 관련 ===

    /**
     * 결제 이력 저장
     */
    public PaymentHistory savePaymentHistory(PaymentHistory history) {
        return paymentHistoryRepository.save(history);
    }

    // === Refund 관련 ===

    /**
     * 환불 저장
     */
    public Refund saveRefund(Refund refund) {
        return refundRepository.save(refund);
    }
}
