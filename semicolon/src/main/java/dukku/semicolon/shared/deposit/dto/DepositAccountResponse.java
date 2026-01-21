package dukku.semicolon.shared.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccountResponse {
    private boolean success;
    private String code;
    private String message;
    private DepositAccountData data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositAccountData {
        private UUID depositUuid;
    }
}
