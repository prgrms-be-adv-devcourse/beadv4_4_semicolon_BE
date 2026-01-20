package dukku.semicolon.shared.deposit.exception;

import dukku.common.global.exception.NotFoundException;

/**
 * 예치금 정보를 찾을 수 없을 때 발생하는 예외
 */
public class DepositNotFoundException extends NotFoundException {
    public DepositNotFoundException() {
        super("예치금 정보를 찾을 수 없습니다.");
    }

    public DepositNotFoundException(String details) {
        super(details);
    }
}
