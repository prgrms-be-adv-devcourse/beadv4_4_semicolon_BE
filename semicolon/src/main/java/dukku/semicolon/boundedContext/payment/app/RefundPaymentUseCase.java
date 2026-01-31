package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.Refund;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import dukku.semicolon.shared.payment.dto.PaymentRefundResponse;
import dukku.common.shared.payment.event.RefundCompletedEvent;
import dukku.semicolon.shared.payment.exception.InvalidRefundAmountException;
import dukku.semicolon.shared.payment.exception.PaymentNotRefundableException;
import dukku.semicolon.boundedContext.payment.out.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import dukku.semicolon.boundedContext.deposit.app.IncreaseDepositUseCase;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 환불 처리 UseCase
 * 
 * <p>
 * 결제 완료된 건에 대해 사용자 요청 또는 시스템 이벤트에 의해 환불을 수행한다.
 * 외부 자산(PG) 취소 성공 후 내부 데이터(예치금/상태) 업데이트를 진행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundPaymentUseCase {

    private final PaymentSupport support;
    private final EventPublisher eventPublisher;
    private final TossPaymentClient tossClient;
    private final IncreaseDepositUseCase increaseDepositUseCase;

    /**
     * 환불 요청 처리
     *
     * @param request        환불 요청 정보
     * @param idempotencyKey 멱등성 키
     * @return 환불 결과
     */
    @Transactional
    public PaymentRefundResponse execute(PaymentRefundRequest request, String idempotencyKey) {
        // 1. 결제 조회
        Payment payment = support.findPaymentByUuid(request.getPaymentId());

        // 2. 환불 가능 상태 검증
        validateRefundable(payment);

        // 3. 환불 금액 검증
        validateRefundAmount(payment, request.getRefundAmount());

        // 4. 상태 변경 전 값 저장 (이력용)
        PaymentStatus originStatus = payment.getPaymentStatus();
        Long originAmountPg = payment.getAmountPg();
        Long originDeposit = payment.getPaymentDeposit();

        // 5. 환불 금액 배분 산정
        // 정책에 따라 예치금 복구액과 PG 취소액을 계산 (엔티티 도메인 로직 활용)
        Payment.RefundAllocation allocation = payment.calculateRefundAllocation(request.getRefundAmount());

        // 6. PG 취소 수행 (외부 시스템 확정 먼저)
        // PG환불이 확정된 다음에 최종적으로 예치금 등 내부 금액을 변동시키는 로직을 수행
        if (allocation.pgRefundAmount() > 0) {
            try {
                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancelReason", request.getReason());
                cancelBody.put("cancelAmount", allocation.pgRefundAmount());

                // TossPaymentClient 내부에서 @Retryable에 의해 장애 시 재시도 로직이 가동된다.
                Map<String, Object> response = tossClient.cancel(payment.getPgPaymentKey(), cancelBody);
                int statusCode = ((Number) response.getOrDefault("statusCode", 200)).intValue();

                if (HttpStatus.valueOf(statusCode).isError()) {
                    log.error("[CRITICAL][취소 오류] PG 취소 실패: status={}, body={}. 관리자 확인 필요! paymentUuid={}",
                            statusCode, response, payment.getUuid());
                    // 최종 실패 시 ROLLBACK_FAILED 상태로 전이하여 관리 포인트로 격리한다.
                    handleFailure(payment, originStatus, originAmountPg, originDeposit,
                            "PG_CANCEL_FAILED: " + response.get("message"));
                    throw new RuntimeException("TOSS_CANCEL_FAILED: " + response.get("message"));
                }
            } catch (Exception e) {
                // 기타 사유로 실패 시
                log.error("[CRITICAL][취소 오류] PG 취소 중 예외 발생: {}. 관리자 확인 필요! paymentUuid={}",
                        e.getMessage(), payment.getUuid());
                handleFailure(payment, originStatus, originAmountPg, originDeposit,
                        "PG_CANCEL_EXCEPTION: " + e.getMessage());
                throw e;
            }
        }

        // 7. 예치금 복구 처리 (PG 성공 후)
        // PG 취소가 성공하거나 PG 취소분 없이 예치금만 환불되는 경우 실행
        if (allocation.depositRefundAmount() > 0) {
            increaseDepositUseCase.increase(
                    payment.getUserUuid(),
                    allocation.depositRefundAmount(),
                    DepositHistoryType.REFUND,
                    null);
        }

        // 8. Refund 엔티티 및 이력 생성
        // 특정 시점의 환불 행위에 대한 영속적 기록을 생성한다.
        Refund refund = payment.createRefund(request.getRefundAmount(), allocation.depositRefundAmount());
        refund.complete();
        support.saveRefund(refund);

        // 9. 결제 상태 및 잔액 업데이트
        payment.partialCancel(request.getRefundAmount(), allocation.pgRefundAmount(), allocation.depositRefundAmount());
        support.savePayment(payment);

        // 10. 이력 생성
        // 결제 전체 상태 변화를 추적할 수 있는 히스토리를 생성
        PaymentHistoryType historyType = (payment.getPaymentStatus() == PaymentStatus.CANCELED)
                ? PaymentHistoryType.FULL_REFUND_SUCCESS
                : PaymentHistoryType.PARTIAL_REFUND_SUCCESS;
        support.createHistory(payment, historyType, originStatus, originAmountPg, originDeposit);

        // 11. 이벤트 발행
        // 주문 취소 확정, 알림 발송 등 후속 동작을 할 리스너들을 위해 이벤트 발행
        eventPublisher.publish(new RefundCompletedEvent(
                refund.getUuid(),
                payment.getUuid(),
                payment.getOrderUuid(),
                request.getRefundAmount(),
                allocation.depositRefundAmount(),
                payment.getUserUuid(),
                refund.getCreatedAt()));

        return refund.toPaymentRefundResponse(allocation.pgRefundAmount(), payment.getTossOrderId());
    }

    // 환불 가능 상태 체크
    private void validateRefundable(Payment payment) {
        PaymentStatus status = payment.getPaymentStatus();
        // DONE, PARTIAL_CANCELED 상태만 환불 가능
        if (status != PaymentStatus.DONE && status != PaymentStatus.PARTIAL_CANCELED) {
            throw new PaymentNotRefundableException();
        }
    }

    // 환불 가능 금액 체크
    private void validateRefundAmount(Payment payment, Long refundAmount) {
        // 환불 가능 금액 = 현재 결제 금액 - 이미 환불된 금액
        Long refundableAmount = payment.getAmount() - payment.getRefundTotal();
        if (refundAmount > refundableAmount) {
            throw new InvalidRefundAmountException("환불 가능 금액 초과: " + refundableAmount);
        }
    }

    // 환불 실패 처리
    private void handleFailure(Payment payment, PaymentStatus originStatus, Long originAmountPg, Long originDeposit,
            String reason) {
        payment.rollbackFailedStatus();
        support.savePayment(payment);
        support.createHistory(payment, PaymentHistoryType.PAYMENT_ROLLBACK_FAILED, originStatus, originAmountPg,
                originDeposit);
    }

}
