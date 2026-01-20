package dukku.semicolon.shared.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 예치금 범용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositDto {
    private UUID userUuid;
    private UUID depositUuid;
    private Long balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
