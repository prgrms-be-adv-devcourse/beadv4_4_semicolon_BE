package dukku.user.shared.user.exception;

import dukku.common.global.exception.ConflictException;

public class UserConflictException extends ConflictException {
    public UserConflictException() {
        super("이미 존재하는 유저 입니다.");
    }
}
