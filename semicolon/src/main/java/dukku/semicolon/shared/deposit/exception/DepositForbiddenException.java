package dukku.semicolon.shared.deposit.exception;

import dukku.common.global.exception.ForbiddenException;

/**
 * 예치금 접근 권한이 없을 때 발생하는 예외 (Forbidden)
 */
public class DepositForbiddenException extends ForbiddenException {
    public DepositForbiddenException() {
        super("예치금 정보에 접근할 권한이 없습니다.");
    }

    public DepositForbiddenException(String details) {
        super(details);
    }
}
