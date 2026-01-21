package dukku.semicolon.shared.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 예치금 잔액 조회 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositBalanceResponse {
    private boolean success;
    private String code;
    private String message;
    private DepositBalanceData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepositBalanceData {
        private UUID userUuid;
        private Long balance;
        private OffsetDateTime updatedAt;
    }
}
