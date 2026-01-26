package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentHistory;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import dukku.semicolon.boundedContext.payment.entity.Refund;
import dukku.semicolon.boundedContext.payment.entity.RefundItem;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import dukku.semicolon.shared.payment.dto.PaymentRefundResponse;
import dukku.common.shared.payment.event.RefundCompletedEvent;
import dukku.semicolon.shared.payment.exception.InvalidRefundAmountException;
import dukku.semicolon.shared.payment.exception.PaymentNotRefundableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 환불 처리 UseCase
 *
 * <p>
 * 결제 취소/환불 요청 처리
 * items가 있으면 부분 환불, 없거나 전체 금액이면 전체 환불로 처리
 */
@Component
@RequiredArgsConstructor
public class RefundPaymentUseCase {

    private final PaymentSupport support;
    private final EventPublisher eventPublisher;

    /**
     * 환불 요청 처리
     *
     * @param request        환불 요청 정보
     * @param idempotencyKey 멱등성 키
     * @return 환불 결과
     */
    public PaymentRefundResponse execute(PaymentRefundRequest request, String idempotencyKey) {
        // 1. 결제 조회
        Payment payment = support.findPaymentByUuid(request.getPaymentId());

        // 2. 환불 가능 상태 검증
        validateRefundable(payment);

        // 3. 환불 금액 검증
        validateRefundAmount(payment, request.getRefundAmount());

        // 4. 상태 변경 전 값 저장
        PaymentStatus originStatus = payment.getPaymentStatus();
        Long originAmountPg = payment.getAmountPg();
        Long originDeposit = payment.getPaymentDeposit();

        // 5. 환불 금액 계산 (예치금 vs PG)
        RefundAmounts refundAmounts = calculateRefundAmounts(payment, request.getRefundAmount());

        // 6. Refund 엔티티 생성
        Refund refund = createRefund(payment, refundAmounts);

        // 6-1. RefundItem 생성 (요청에 아이템 정보가 있는 경우)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PaymentRefundRequest.RefundItemInfo itemInfo : request.getItems()) {
                PaymentOrderItem orderItem = support.findPaymentOrderItem(payment.getId(), itemInfo.getOrderItemUuid())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "결제 내역에서 해당 상품을 찾을 수 없습니다: " + itemInfo.getOrderItemUuid()));

                RefundItem refundItem = RefundItem.create(refund, orderItem,
                        itemInfo.getRefundAmount(), 0L, itemInfo.getRefundAmount());
                refund.addRefundItem(refundItem);
            }
        }

        // 7. 결제 상태 업데이트
        updatePaymentStatus(payment, request.getRefundAmount());

        // 8. 이력 생성 (Full vs Partial 구분)
        PaymentHistoryType historyType = payment.getPaymentStatus() == PaymentStatus.CANCELED
                ? PaymentHistoryType.FULL_REFUND_SUCCESS
                : PaymentHistoryType.PARTIAL_REFUND_SUCCESS;

        support.createHistory(payment, historyType, originStatus, originAmountPg, originDeposit);

        // 9. 이벤트 발행
        eventPublisher.publish(new RefundCompletedEvent(
                refund.getUuid(),
                payment.getUuid(),
                payment.getOrderUuid(),
                refundAmounts.totalAmount,
                refundAmounts.depositAmount,
                payment.getUserUuid(),
                refund.getCreatedAt()));

        // 10. 응답 생성
        return refund.toPaymentRefundResponse(refundAmounts.pgAmount, payment.getTossOrderId());
    }

    private void validateRefundable(Payment payment) {
        PaymentStatus status = payment.getPaymentStatus();
        // DONE, PARTIAL_CANCELED 상태만 환불 가능
        if (status != PaymentStatus.DONE && status != PaymentStatus.PARTIAL_CANCELED) {
            throw new PaymentNotRefundableException();
        }
    }

    private void validateRefundAmount(Payment payment, Long refundAmount) {
        // 환불 가능 금액 = 현재 결제 금액 - 이미 환불된 금액
        Long refundableAmount = payment.getAmount() - payment.getRefundTotal();
        if (refundAmount > refundableAmount) {
            throw new InvalidRefundAmountException(
                    "환불 가능 금액(" + refundableAmount + ")을 초과했습니다. 요청: " + refundAmount);
        }
    }

    private RefundAmounts calculateRefundAmounts(Payment payment, Long totalRefundAmount) {
        // 환불 시 예치금 우선 환불 정책
        // 남은 예치금 사용액 중에서 환불
        Long remainingDeposit = payment.getPaymentDeposit();
        Long depositRefund = Math.min(remainingDeposit, totalRefundAmount);
        Long pgRefund = totalRefundAmount - depositRefund;

        return new RefundAmounts(depositRefund, pgRefund, totalRefundAmount);
    }

    private Refund createRefund(Payment payment, RefundAmounts amounts) {
        Refund refund = Refund.create(
                payment,
                amounts.totalAmount,
                amounts.depositAmount);
        // 바로 완료 처리로 우선 구현, 토스페이먼츠 실제 API 연동하면서 실제 취소 로직으로 변경
        // TODO: 실제 PG 취소 API 호출로 변경
        refund.complete();
        payment.addRefund(refund);
        return support.saveRefund(refund);
    }

    private void updatePaymentStatus(Payment payment, Long refundAmount) {
        // 전체 환불인지 부분 환불인지 판단
        Long totalPaid = payment.getAmount();
        Long totalRefunded = payment.getRefundTotal() + refundAmount;

        if (totalRefunded.equals(totalPaid)) {
            payment.cancel();
        } else {
            payment.partialCancel(refundAmount);
        }
        support.savePayment(payment);
    }

    /**
     * 환불 금액 계산 결과
     */
    private record RefundAmounts(Long depositAmount, Long pgAmount, Long totalAmount) {
    }
}
