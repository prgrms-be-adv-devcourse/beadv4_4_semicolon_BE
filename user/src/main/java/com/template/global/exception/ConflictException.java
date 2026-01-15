package com.template.global.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String details) {
        super(HttpStatus.CONFLICT.getReasonPhrase(), "중복된 데이터로 인해 충돌이 발생했습니다.", HttpStatus.CONFLICT, details);
    }
}