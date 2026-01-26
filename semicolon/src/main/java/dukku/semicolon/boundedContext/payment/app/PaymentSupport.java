package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import dukku.semicolon.boundedContext.payment.entity.Refund;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.boundedContext.payment.out.PaymentHistoryRepository;
import dukku.semicolon.boundedContext.payment.out.PaymentOrderItemRepository;
import dukku.semicolon.boundedContext.payment.out.PaymentRepository;
import dukku.semicolon.boundedContext.payment.out.RefundRepository;
import dukku.semicolon.shared.payment.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.CannotAcquireLockException;

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
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final RefundRepository refundRepository;
    private final PaymentOrderItemRepository paymentOrderItemRepository;

    // === Payment 관련 ===

    /**
     * UUID로 결제 조회 (없으면 예외)
     */
    public Payment findPaymentByUuid(UUID uuid) {
        return paymentRepository.findByUuid(uuid)
                .orElseThrow(PaymentNotFoundException::new);
    }

    /**
     * 결제 아이템 조회 (Payment ID + Order Item UUID)
     */
    public Optional<PaymentOrderItem> findPaymentOrderItem(int paymentId, UUID orderItemUuid) {
        return paymentOrderItemRepository.findByPaymentIdAndOrderItemUuid(paymentId, orderItemUuid);
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
     * 주문 UUID로 결제 목록 조회
     */
    public List<Payment> findPaymentsByOrderUuid(UUID orderUuid) {
        return paymentRepository.findByOrderUuid(orderUuid);
    }

    /**
     * PG 결제키로 결제 조회
     */
    public Optional<Payment> findPaymentByPgPaymentKey(String pgPaymentKey) {
        return paymentRepository.findByPgPaymentKey(pgPaymentKey);
    }

    /**
     * 결제 저장 (재시도 적용)
     */
    @Retryable(retryFor = { DataAccessException.class,
            CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    // === PaymentHistory 관련 ===

    /**
     * 결제 이력 생성 및 저장 (통합형)
     * 
     * @param payment       가장 최신 상태의 Payment 엔티티
     * @param type          이력 유형 (REQUESTED, SUCCESS, FAILED 등)
     * @param originStatus  변경 전 상태 (요청 시엔 null)
     * @param originPg      변경 전 PG 금액
     * @param originDeposit 변경 전 예치금액
     */
    @Retryable(retryFor = { DataAccessException.class,
            CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void createHistory(Payment payment, PaymentHistoryType type,
            PaymentStatus originStatus, Long originPg, Long originDeposit) {
        PaymentHistory history = PaymentHistory.create(
                payment,
                type,
                originStatus,
                payment.getPaymentStatus(),
                originPg,
                payment.getAmountPg(),
                originDeposit,
                payment.getPaymentDeposit());
        paymentHistoryRepository.save(history);
    }

    /**
     * @deprecated 직접 엔티티를 조립하기보다 createHistory 사용 권장
     */
    @Deprecated
    public PaymentHistory savePaymentHistory(PaymentHistory history) {
        return paymentHistoryRepository.save(history);
    }

    // === Refund 관련 ===

    /**
     * 환불 저장 (재시도 적용)
     */
    @Retryable(retryFor = { DataAccessException.class,
            CannotAcquireLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Refund saveRefund(Refund refund) {
        return refundRepository.save(refund);
    }
}
