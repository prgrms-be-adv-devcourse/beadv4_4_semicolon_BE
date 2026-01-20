package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.ConflictException;

/**
 * 결제 상태가 요청한 작업에 적합하지 않을 때 발생하는 예외
 *
 * <p>
 * 상태 전이 규칙을 위반한 작업 시도 시 발생
 */
public class WrongPaymentStatusException extends ConflictException {
    public WrongPaymentStatusException() {
        super("현재 결제 상태에서는 해당 작업을 수행할 수 없습니다.");
    }

    public WrongPaymentStatusException(String details) {
        super(details);
    }

    public WrongPaymentStatusException(String currentStatus, String targetAction) {
        super(String.format("결제 상태 불일치: 현재 상태(%s)에서는 [%s] 작업을 할 수 없습니다.", currentStatus, targetAction));
    }
}
