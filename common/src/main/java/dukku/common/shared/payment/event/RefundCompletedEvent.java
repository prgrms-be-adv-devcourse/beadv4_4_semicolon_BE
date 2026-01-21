package dukku.common.shared.payment.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 환불 완료 이벤트
 * <p>
 * 환불 처리가 완료되었을 때 발행.
 * 예치금 롤백 및 주문/상품 상태 변경의 트리거가 됩니다.
 */
public record RefundCompletedEvent(
        UUID refundId,
        UUID paymentId,
        UUID orderUuid,
        Long refundAmount,
        Long refundDepositAmount, // 환불된 예치금
        UUID userUuid,
        LocalDateTime occurredAt
) {
}
