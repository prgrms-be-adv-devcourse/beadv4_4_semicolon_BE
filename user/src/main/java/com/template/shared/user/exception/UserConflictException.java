package com.template.shared.user.exception;

import com.template.global.exception.ConflictException;

public class UserConflictException extends ConflictException {
    public UserConflictException() {
        super("이미 존재하는 유저 입니다.");
    }
}
