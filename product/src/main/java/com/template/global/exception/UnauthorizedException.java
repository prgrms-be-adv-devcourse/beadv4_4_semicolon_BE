package com.template.global.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String details) {
        super(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "잘못된 인증 정보입니다.", HttpStatus.UNAUTHORIZED, details);
    }
}
