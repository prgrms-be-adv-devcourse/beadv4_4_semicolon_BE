package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.common.shared.payment.type.PaymentHistoryType;
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

        // 2. 상태 검증 (PENDING 상태인 경우에만 승인 가능)
        validatePaymentStatus(payment);

        // 3. 금액 유효성 검증 (토스 결제 요청 금액 vs 시스템 내 결제 대상 금액)
        validateAmount(payment, request.getToss().getAmount());

        // 4. 결제 키 중복 검증 (이미 처리된 결제인지 확인)
        validateDuplicatePaymentKey(request.getToss().getPaymentKey());

        // 5. 결제 승인 상태 변경 전 값 저장 (이력 기록용)
        PaymentStatus originStatus = payment.getPaymentStatus();
        Long originAmountPg = payment.getAmountPg();
        Long originDeposit = payment.getPaymentDeposit();

        // 6. 실제 토스페이먼츠 승인 요청 API 호출
        java.util.Map<String, Object> tossRequestBody = new java.util.HashMap<>();
        tossRequestBody.put("paymentKey", request.getToss().getPaymentKey());
        tossRequestBody.put("orderId", request.getToss().getOrderId());
        tossRequestBody.put("amount", request.getToss().getAmount());

        java.util.Map<String, Object> tossResponse = tossClient.confirm(tossRequestBody);
        int statusCode = ((Number) tossResponse.getOrDefault("statusCode", 200)).intValue();

        if (statusCode >= 400) {
            log.error("[Toss Confirm API Error] status={}, body={}", statusCode, tossResponse);
            // 실패 이력 기록
            support.createHistory(payment, PaymentHistoryType.PAYMENT_FAILED, originStatus, originAmountPg,
                    originDeposit);
            return payment.toPaymentConfirmResponse(false, "PG 승인 실패: " + tossResponse.get("message"));
        }

        // 7. 시스템 내 결제 승인 (상태 변경 및 결제키 저장)
        payment.approve(request.getToss().getPaymentKey());
        support.savePayment(payment);

        // 8. 결제 성공 이력 생성
        support.createHistory(payment, PaymentHistoryType.PAYMENT_SUCCESS, originStatus, originAmountPg, originDeposit);

        // 9. 예치금 차감
        // Deposit BC에서 상품별로 정확히 예치금을 차감하고 이력을 남길 수 있도록
        // 각 상품 스냅샷에서 예치금 사용액이 있는 아이템만 필터링하여 정보를 가공
        // 가공된 항목을 List에 추가
        java.util.List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages = payment.getItems().stream()
                .filter(item -> item.getPaymentDeposit() != null && item.getPaymentDeposit() > 0)
                .map(item -> new PaymentSuccessEvent.ItemDepositUsage(item.getOrderItemUuid(),
                        item.getPaymentDeposit()))
                .toList();

        // 10. 결제 성공 이벤트 발행 (주문 상태 변경 및 예치금 차감 트리거)
        eventPublisher.publish(new PaymentSuccessEvent(
                payment.getUuid(),
                payment.getUuid(), // paymentUuid (2026-01-24 추가된 필드 대응)
                payment.getOrderUuid(),
                payment.getAmount(),
                payment.getPaymentDeposit(),
                payment.getUserUuid(),
                payment.getApprovedAt(),
                itemDepositUsages));

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
}
