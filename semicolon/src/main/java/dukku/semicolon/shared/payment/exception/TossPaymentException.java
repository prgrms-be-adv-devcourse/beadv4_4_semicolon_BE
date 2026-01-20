package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * 토스페이먼츠 API 연동 중 발생하는 예외
 *
 * <p>
 * PG사에서 내려주는 에러 코드와 메시지를 그대로 전달
 */
public class TossPaymentException extends BaseException {

    public TossPaymentException(String code, String message) {
        super(code, message, HttpStatus.BAD_GATEWAY, null);
    }

    public TossPaymentException(String code, String message, String details) {
        super(code, message, HttpStatus.BAD_GATEWAY, details);
    }
}
