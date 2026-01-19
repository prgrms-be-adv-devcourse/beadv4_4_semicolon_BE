package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.ConflictException;

/**
 * 결제 상태가 PENDING이 아닐 때 승인 요청이 들어온 경우 발생하는 예외
 *
 * <p>
 * 승인 대기 상태에서만 결제 확정(confirm) 가능
 */
public class PaymentNotPendingException extends ConflictException {
    public PaymentNotPendingException() {
        super("결제 상태가 승인 대기(PENDING)가 아닙니다.");
    }

    public PaymentNotPendingException(String currentStatus) {
        super(String.format("결제 승인 불가: 현재 상태가 %s입니다. PENDING 상태에서만 승인 가능합니다.", currentStatus));
    }
}
