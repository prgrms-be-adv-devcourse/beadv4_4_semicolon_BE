package dukku.semicolon.boundedContext.deposit.exception;

import dukku.common.global.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * 잔액 부족 예외
 *
 * <p>
 * 결제 사용 또는 차감 시도시 예치금 잔액이 부족한 경우 발생한다.
 * HTTP 400 Bad Request를 반환한다.
 */
public class NotEnoughDepositException extends BaseException {

    /**
     * 예외 생성자
     */
    public NotEnoughDepositException() {
        super("NOT_ENOUGH_DEPOSIT", "예치금이 부족합니다.", HttpStatus.BAD_REQUEST, null);
    }
}
