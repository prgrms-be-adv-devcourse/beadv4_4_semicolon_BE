package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.NotFoundException;

/**
 * 결제 정보를 찾을 수 없을 때 발생하는 예외
 *
 * <p>
 * 존재하지 않는 paymentUuid 조회 시 발생
 */
public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException() {
        super("결제 내역을 찾을 수 없습니다.");
    }

    public PaymentNotFoundException(String details) {
        super(details);
    }
}
