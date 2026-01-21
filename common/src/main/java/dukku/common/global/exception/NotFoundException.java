package dukku.common.global.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String details) {
        super(HttpStatus.NOT_FOUND.getReasonPhrase(), "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, details);
    }
}
