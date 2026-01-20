package dukku.semicolon.shared.payment.exception;

import dukku.common.global.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * PG사 결제 승인 실패 시 발생하는 예외
 *
 * <p>
 * 토스페이먼츠 Confirm API 호출 실패 등
 */
public class PaymentConfirmFailedException extends BaseException {
    public PaymentConfirmFailedException() {
        super("PAYMENT_CONFIRM_FAILED", "결제 승인에 실패했습니다.", HttpStatus.BAD_GATEWAY, null);
    }

    public PaymentConfirmFailedException(String pgCode, String pgMessage) {
        super("PAYMENT_CONFIRM_FAILED", "결제 승인에 실패했습니다.", HttpStatus.BAD_GATEWAY,
                String.format("PG 응답 - code: %s, message: %s", pgCode, pgMessage));
    }

    // 참조 : 토스페이먼츠 에러코드
    // https://docs.tosspayments.com/reference/error-codes
    // PG사 응답에러는 PG사 스펙에 따른 코드와 메시지 전달

}
