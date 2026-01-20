package dukku.semicolon.shared.settlement.dto;

import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 정산 목록 검색 요청 DTO
 * Controller에서 바인딩 + Validation 담당
 */
public record SettlementSearchRequest(
        SettlementStatus status,
        UUID sellerUuid,
        @PastOrPresent(message = "시작일은 현재 또는 과거 날짜여야 합니다")
        LocalDate startDate,
        @PastOrPresent(message = "종료일은 현재 또는 과거 날짜여야 합니다")
        LocalDate endDate
) {
    /**
     * Repository 검색 조건으로 변환
     */
    public SettlementSearchCondition toCondition() {
        return new SettlementSearchCondition(status, sellerUuid, startDate, endDate);
    }
}
