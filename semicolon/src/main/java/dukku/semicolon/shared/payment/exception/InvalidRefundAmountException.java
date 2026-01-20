package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BadRequestException;

/**
 * 환불 금액이 유효하지 않을 때 발생하는 예외
 *
 * <p>
 * 잔여 환불 가능 금액 초과 등
 */
public class InvalidRefundAmountException extends BadRequestException {
    public InvalidRefundAmountException() {
        super("유효하지 않은 환불 금액입니다.");
    }

    public InvalidRefundAmountException(String details) {
        super(details);
    }

    public InvalidRefundAmountException(Long requested, Long available) {
        super(String.format("환불 금액 초과: 요청(%d원), 가능(%d원)", requested, available));
    }
}
