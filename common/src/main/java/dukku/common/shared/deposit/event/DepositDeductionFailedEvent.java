package dukku.common.shared.deposit.event;

import java.util.UUID;

/**
 * 예치금 차감 실패 이벤트
 *
 * <p>
 * 예치금 잔액 부족 등의 이유로 차감이 실패했을 때 발행.
 * Payment BC는 이 이벤트를 수신하여 보상 트랜잭션(결제 취소)을 수행해야 함.
 */
public record DepositDeductionFailedEvent(
        UUID orderUuid,
        UUID userUuid,
        Long amount,
        String reason) {
}
