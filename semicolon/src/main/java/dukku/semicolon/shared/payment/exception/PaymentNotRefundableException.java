package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.ConflictException;

/**
 * 환불 불가 상태일 때 발생하는 예외
 *
 * <p>
 * 이미 전액 환불되었거나, 환불 불가 상태인 경우 발생
 */
public class PaymentNotRefundableException extends ConflictException {
    public PaymentNotRefundableException() {
        super("해당 결제는 환불할 수 없는 상태입니다.");
    }

    public PaymentNotRefundableException(String details) {
        super(details);
    }
}
