package com.template.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String details)
    {
        super(HttpStatus.FORBIDDEN.getReasonPhrase(), "잘못된 요청", HttpStatus.FORBIDDEN, details );
    }
}
