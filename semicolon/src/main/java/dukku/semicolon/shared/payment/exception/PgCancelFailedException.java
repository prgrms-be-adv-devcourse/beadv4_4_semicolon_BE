package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * PG사 결제 취소 실패 시 발생하는 예외
 *
 * <p>
 * 토스페이먼츠 Cancel API 호출 실패 등
 */
public class PgCancelFailedException extends BaseException {
    public PgCancelFailedException() {
        super("PG_CANCEL_FAILED", "PG 결제 취소에 실패했습니다.", HttpStatus.BAD_GATEWAY, null);
    }

    public PgCancelFailedException(String pgCode, String pgMessage) {
        super("PG_CANCEL_FAILED", "PG 결제 취소에 실패했습니다.", HttpStatus.BAD_GATEWAY,
                String.format("PG 응답 - code: %s, message: %s", pgCode, pgMessage));
    }
}
