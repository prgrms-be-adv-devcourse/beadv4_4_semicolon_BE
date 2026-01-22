package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.shared.payment.dto.PaymentConfirmRequest;
import dukku.semicolon.shared.payment.dto.PaymentConfirmResponse;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import dukku.semicolon.shared.payment.dto.PaymentRefundResponse;
import dukku.semicolon.shared.payment.dto.PaymentRequest;
import dukku.semicolon.shared.payment.dto.PaymentResponse;
import dukku.semicolon.shared.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Payment 도메인 Facade
 *
 * <p>
 * Controller가 호출하는 진입점
 *
 * @see RequestPaymentUseCase 결제 요청
 * @see ConfirmPaymentUseCase 결제 승인
 * @see FindPaymentUseCase 결제 조회
 * @see RefundPaymentUseCase 환불 처리
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentFacade {

    private final RequestPaymentUseCase requestPayment;
    private final ConfirmPaymentUseCase confirmPayment;
    private final FindPaymentUseCase findPayment;
    private final RefundPaymentUseCase refundPayment;

    /**
     * 결제 요청 (준비)
     *
     * <p>
     * 프론트에서 결제 준비 요청 시 토스 결제창 호출에 필요한 정보 반환
     *
     * @param request        결제 요청 정보
     * @param idempotencyKey 멱등성 키
     * @return 토스 결제창 호출 정보
     */
    public PaymentResponse requestPayment(PaymentRequest request, String idempotencyKey) {
        return requestPayment.execute(request, idempotencyKey);
    }

    /**
     * 결제 승인 확정
     *
     * <p>
     * 토스 인증 완료 후 백엔드에서 최종 승인처리
     *
     * @param request        토스 인증 정보
     * @param idempotencyKey 멱등성 키
     * @return 승인 결과
     */
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request, String idempotencyKey) {
        return confirmPayment.execute(request, idempotencyKey);
    }

    /**
     * 결제 내역 조회
     *
     * @param paymentUuid 결제 UUID
     * @return 결제 상세 정보
     */
    @Transactional(readOnly = true)
    public PaymentResultResponse findPaymentResult(UUID paymentUuid) {
        return findPayment.execute(paymentUuid).toPaymentResultResponse();
    }

    /**
     * 환불 요청
     *
     * @param request        환불 요청 정보
     * @param idempotencyKey 멱등성 키
     * @return 환불 결과
     */
    public PaymentRefundResponse refundPayment(PaymentRefundRequest request, String idempotencyKey) {
        return refundPayment.execute(request, idempotencyKey);
    }
}
