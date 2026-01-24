package dukku.semicolon.shared.settlement.dto;

import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 정산 데이터 전송 객체 (순수 정산 정보만)
 * - Settlement 엔티티의 데이터만 포함
 * - 외부 Bounded Context 정보는 포함하지 않음
 * - API Client를 통한 BC 간 통신에 사용
 * - 배치 처리, 내부 UseCase에서 사용
 */
@Builder
public record SettlementDto(
        Long id,
        UUID uuid,
        UUID sellerUuid,
        UUID buyerUuid,
        UUID paymentId,
        UUID orderId,
        UUID orderItemId,
        UUID depositId,
        SettlementStatus status,
        Long totalAmount,
        BigDecimal fee,
        Long feeAmount,
        Long settlementAmount,
        LocalDateTime settlementReservationDate,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
