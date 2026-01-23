package dukku.common.shared.settlement.event;

import java.util.UUID;

/**
 * 정산 지급 요청 이벤트
 *
 * @param userUuid       대상 사용자 UUID
 * @param amount         지급 금액
 * @param settlementUuid 정산 UUID
 */
public record SettlementPayoutRequestedEvent(
        UUID userUuid,
        Long amount,
        UUID settlementUuid) {
}
