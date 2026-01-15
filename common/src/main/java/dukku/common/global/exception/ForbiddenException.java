package dukku.common.global.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String details)
    {
        super(HttpStatus.FORBIDDEN.getReasonPhrase(), "권한이 없습니다.", HttpStatus.FORBIDDEN, details );
    }
}
