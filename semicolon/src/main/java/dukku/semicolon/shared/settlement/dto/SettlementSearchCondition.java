package dukku.semicolon.shared.settlement.dto;

import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 정산 목록 검색 조건
 * 모든 조건 필드가 null이면 전체 조회
 */
public record SettlementSearchCondition(
        SettlementStatus status,
        UUID sellerUuid,
        LocalDate startDate,
        LocalDate endDate
) {
    public static SettlementStatisticsCondition empty() {
        return new SettlementStatisticsCondition(null, null, null, null);
    }
}
