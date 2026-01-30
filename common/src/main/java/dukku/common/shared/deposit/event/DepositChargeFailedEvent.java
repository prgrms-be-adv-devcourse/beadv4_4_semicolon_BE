package dukku.common.shared.deposit.event;

import java.util.UUID;

// 예치금 충전 실패 이벤트
public record DepositChargeFailedEvent(
                UUID userUuid,
                Long amount,
                UUID settlementUuid,
                String reason,
                String hello
) {
}
