package dukku.common.shared.deposit.event;

import java.util.UUID;

/**
 * 예치금 환불 성공 이벤트
 *
 * <p>
 * 환불 절차에 따라 예치금이 정상적으로 복구(재적립)되었을 때 발행.
 * 알림 서비스 등에서 사용.
 */
public record DepositRefundedEvent(
        UUID orderUuid,
        UUID userUuid,
        Long amount) {
}
