package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentHistoryType;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentConfirmRequest;
import dukku.semicolon.shared.payment.dto.PaymentConfirmResponse;
import dukku.semicolon.shared.payment.exception.DuplicatePaymentKeyException;
import dukku.semicolon.shared.payment.exception.PaymentNotPendingException;
import dukku.semicolon.shared.payment.exception.TossAmountMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * 결제 승인 확정 UseCase
 *
 * <p>
 * 토스페이먼츠 인증 완료 후 백엔드에서 최종 승인 처리
 * {@link Payment#approve(String)} 호출하여 결제 확정
 *
 * <p>
 * TODO: 실제 토스페이먼츠 API 호출은 별도 태스크로 추후 구현
 */
@Component
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final PaymentSupport support;

    /**
     * 결제 승인 확정 처리
     *
     * @param request        토스 인증 정보
     * @param idempotencyKey 멱등성 키
     * @return 결제 승인 결과
     */
    public PaymentConfirmResponse execute(PaymentConfirmRequest request, String idempotencyKey) {
        // 1. 결제 조회
        Payment payment = support.findPaymentByUuid(request.getPaymentUuid());

        // 2. 상태 검증 (PENDING만 승인 가능)
        validatePaymentStatus(payment);

        // 3. 금액 검증 (토스 amount vs 내부 pgAmount)
        validateAmount(payment, request.getToss().getAmount());

        // 4. 중복 paymentKey 검증
        validateDuplicatePaymentKey(request.getToss().getPaymentKey());

        // 5. 결제 승인 상태 변경 전 값 저장
        PaymentStatus originStatus = payment.getPaymentStatus();
        Long originAmountPg = payment.getAmountPg();
        Long originDeposit = payment.getPaymentDeposit();

        // 6. 결제 승인 처리
        // TODO: 실제 토스 API 호출
        payment.approve(request.getToss().getPaymentKey());
        support.savePayment(payment);

        // 7. 이력 생성
        createHistory(payment, originStatus, originAmountPg, originDeposit);

        // TODO: PaymentCompletedEvent 이벤트 발행 (Deposit BC 연동용)

        // 8. 응답 생성
        return buildResponse(payment, request);
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new PaymentNotPendingException();
        }
    }

    private void validateAmount(Payment payment, Long tossAmount) {
        if (!payment.getAmountPg().equals(tossAmount)) {
            throw new TossAmountMismatchException(payment.getAmountPg(), tossAmount);
        }
    }

    private void validateDuplicatePaymentKey(String paymentKey) {
        if (support.findPaymentByPgPaymentKey(paymentKey).isPresent()) {
            throw new DuplicatePaymentKeyException();
        }
    }

    private void createHistory(Payment payment, PaymentStatus originStatus,
            Long originAmountPg, Long originDeposit) {
        PaymentHistory history = PaymentHistory.create(
                payment,
                PaymentHistoryType.PAYMENT_SUCCESS,
                originStatus,
                payment.getPaymentStatus(),
                originAmountPg,
                payment.getAmountPg(),
                originDeposit,
                payment.getPaymentDeposit());
        support.savePaymentHistory(history);
    }

    private PaymentConfirmResponse buildResponse(Payment payment, PaymentConfirmRequest request) {
        return PaymentConfirmResponse.builder()
                .success(true)
                .code("PAYMENT_CONFIRMED")
                .message("결제가 승인되었습니다.")
                .data(PaymentConfirmResponse.PaymentConfirmData.builder()
                        .paymentUuid(payment.getUuid())
                        .status(payment.getPaymentStatus())
                        .approvedAt(OffsetDateTime.now())
                        .toss(PaymentConfirmResponse.TossInfo.builder()
                                .orderId(request.getToss().getOrderId())
                                .paymentKey(request.getToss().getPaymentKey())
                                .build())
                        .amounts(PaymentConfirmResponse.AmountInfo.builder()
                                .finalPayAmount(payment.getAmount())
                                .depositUseAmount(payment.getPaymentDeposit())
                                .pgPayAmount(payment.getAmountPg())
                                .build())
                        .build())
                .build();
    }
}
