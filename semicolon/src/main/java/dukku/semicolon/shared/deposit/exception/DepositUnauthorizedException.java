package dukku.semicolon.shared.deposit.exception;

import dukku.common.global.exception.UnauthorizedException;

/**
 * 예치금 조회 시 인증 실패 예외
 */
public class DepositUnauthorizedException extends UnauthorizedException {
    public DepositUnauthorizedException() {
        super("인증이 필요하거나 유효하지 않습니다.");
    }

    public DepositUnauthorizedException(String details) {
        super(details);
    }
}
