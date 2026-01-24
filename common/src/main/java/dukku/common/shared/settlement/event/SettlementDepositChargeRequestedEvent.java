package dukku.common.shared.settlement.event;

import java.util.UUID;

/**
 * 정산 지급 요청 이벤트
 * - 배치 작업에서 판매자에게 정산금을 지급하기 위해 발행
 * - Deposit BC에서 수신하여 예치금 충전 처리
 */
public record SettlementDepositChargeRequestedEvent(
        UUID userUuid,         // 판매자 UUID
        Long amount,           // 정산 금액
        UUID settlementUuid    // 정산 UUID
) {
}
