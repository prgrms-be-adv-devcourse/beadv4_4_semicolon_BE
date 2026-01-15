package dukku.common.global.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    public ValidationException(String details) {
        super("VALIDATION_ERROR", "입력 데이터에 오류가 있습니다.", HttpStatus.UNPROCESSABLE_ENTITY, details);
    }
}

