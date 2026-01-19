package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.ConflictException;

/**
 * 중복된 결제 요청 시 발생하는 예외
 *
 * <p>
 * 동일한 멱등성 키로 이미 처리가 완료되었거나 진행 중인 경우 발생
 */
public class DuplicatePaymentKeyException extends ConflictException {
    public DuplicatePaymentKeyException() {
        super("이미 처리된 결제 요청입니다.");
    }

    public DuplicatePaymentKeyException(String details) {
        super(details);
    }
}
