package dukku.semicolon.boundedContext.user.exception;

import dukku.common.global.exception.ConflictException;

public class AlreadyWithdrawUserException extends ConflictException {

    public AlreadyWithdrawUserException() {
        super("이미 탈퇴한 회원입니다.");
    }
}
