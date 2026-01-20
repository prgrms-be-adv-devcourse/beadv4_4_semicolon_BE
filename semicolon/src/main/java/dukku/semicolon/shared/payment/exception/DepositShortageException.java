package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BadRequestException;

/**
 * 예치금 잔액 부족 시 발생하는 예외
 *
 * <p>
 * 결제 시 사용하려는 예치금이 현재 잔액보다 클 때 발생
 */
public class DepositShortageException extends BadRequestException {
    public DepositShortageException() {
        super("예치금 잔액이 부족합니다.");
    }

    public DepositShortageException(String details) {
        super(details);
    }

    public DepositShortageException(Long requested, Long available) {
        super(String.format("예치금 잔액이 부족합니다. (요청: %d원, 잔액: %d원)", requested, available));
    }
}
