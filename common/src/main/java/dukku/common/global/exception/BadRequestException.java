package dukku.common.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String details)
    {
        super(HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청", HttpStatus.BAD_REQUEST, details );
    }
}
