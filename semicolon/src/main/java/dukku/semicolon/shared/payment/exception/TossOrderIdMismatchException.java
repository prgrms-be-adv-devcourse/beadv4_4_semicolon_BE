package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BadRequestException;

/**
 * 토스 orderId 불일치 시 발생하는 예외
 *
 * <p>
 * 결제 승인 요청 시 백엔드에 저장된 orderId와 요청 orderId가 다를 때 발생
 */
public class TossOrderIdMismatchException extends BadRequestException {
    public TossOrderIdMismatchException() {
        super("토스 주문 ID가 일치하지 않습니다.");
    }

    public TossOrderIdMismatchException(String expected, String actual) {
        super(String.format("토스 orderId 불일치: 기대값(%s), 실제값(%s)", expected, actual));
    }
}
