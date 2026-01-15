package com.template.shared.user.exception;


import com.template.global.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("존재하지 않는 유저입니다.");
    }
}
