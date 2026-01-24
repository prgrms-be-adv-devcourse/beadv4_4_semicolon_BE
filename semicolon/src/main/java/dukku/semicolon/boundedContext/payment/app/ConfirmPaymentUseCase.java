package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentConfirmRequest;
import dukku.semicolon.shared.payment.dto.PaymentConfirmResponse;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.semicolon.shared.payment.exception.DuplicatePaymentKeyException;
import dukku.semicolon.shared.payment.exception.PaymentNotPendingException;
import dukku.semicolon.shared.payment.exception.TossAmountMismatchException;
import dukku.semicolon.boundedContext.payment.out.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * 결제 승인 확정 UseCase
 *
 * <p>
 * 토스페이먼츠 인증 완료 후 백엔드에서 최종 승인 처리
 * {@link Payment#approve(String)} 호출하여 결제 확정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final PaymentSupport support;
    private final TossPaymentClient tossClient;
    private final EventPublisher eventPublisher;

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

        // 6. 실제 토스 API 호출 (Client 위임)
        java.util.Map<String, Object> tossRequestBody = new java.util.HashMap<>();
        tossRequestBody.put("paymentKey", request.getToss().getPaymentKey());
        tossRequestBody.put("orderId", request.getToss().getOrderId());
        tossRequestBody.put("amount", request.getToss().getAmount());

        java.util.Map<String, Object> tossResponse = tossClient.confirm(tossRequestBody);
        int statusCode = ((Number) tossResponse.getOrDefault("statusCode", 200)).intValue();

        if (statusCode >= 400) {
            log.error("[Toss Confirm API Error] status={}, body={}", statusCode, tossResponse);
            // 에러 시 로직 (명세에 따라 상세 처리 가능)
            return payment.toPaymentConfirmResponse(false, "PG 승인 실패: " + tossResponse.get("message"));
        }

        // 7. 결제 승인 처리
        payment.approve(request.getToss().getPaymentKey());
        support.savePayment(payment);

        // 7. 이력 생성
        createHistory(payment, originStatus, originAmountPg, originDeposit);

        // 8. 이벤트 발행
        eventPublisher.publish(new PaymentSuccessEvent(
                payment.getUuid(),
                payment.getOrderUuid(),
                payment.getAmount(),
                payment.getPaymentDeposit(),
                payment.getUserUuid(),
                payment.getApprovedAt()));

        // 8. 응답 생성
        return payment.toPaymentConfirmResponse(true, "결제가 승인되었습니다.");
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
}
