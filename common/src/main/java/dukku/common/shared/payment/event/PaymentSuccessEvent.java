package dukku.common.shared.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 완료 이벤트
 * <p>
 * 결제가 성공적으로 완료되었을 때 발행.
 * 예치금 차감 및 주문 상태 변경의 트리거가 됩니다.
 * (Common 모듈의 Event는 Record 타입을 사용하는 컨벤션을 따름)
 */
public record PaymentSuccessEvent(
        UUID paymentId,
        UUID orderUuid,
        Long amount,
        Long paymentDeposit, // 사용된 예치금
        UUID userUuid,
        LocalDateTime occurredAt) {
}
