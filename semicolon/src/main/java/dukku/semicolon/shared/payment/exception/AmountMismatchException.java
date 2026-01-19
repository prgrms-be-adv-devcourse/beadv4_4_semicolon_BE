package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BadRequestException;

/**
 * 결제 요청 금액과 실제 계산된 금액이 다를 때 발생하는 예외
 *
 * <p>
 * 주문 금액 검증 실패 시 발생
 */
public class AmountMismatchException extends BadRequestException {
    public AmountMismatchException() {
        super("결제 금액이 일치하지 않습니다.");
    }

    public AmountMismatchException(String details) {
        super(details);
    }

    public AmountMismatchException(Integer expected, Integer actual) {
        super(String.format("결제 금액 불일치: 기대값(%d원), 실제값(%d원)", expected, actual));
    }
}
