package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BadRequestException;

/**
 * 토스 금액 불일치 시 발생하는 예외
 *
 * <p>
 * 결제 승인 요청 시 백엔드에 저장된 amount와 요청 amount가 다를 때 발생
 */
public class TossAmountMismatchException extends BadRequestException {
    public TossAmountMismatchException() {
        super("토스 결제 금액이 일치하지 않습니다.");
    }

    public TossAmountMismatchException(Integer expected, Integer actual) {
        super(String.format("토스 amount 불일치: 기대값(%d원), 실제값(%d원)", expected, actual));
    }
}
