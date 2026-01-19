package dukku.semicolon.shared.deposit.dto;

import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 예치금 이력 범용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositHistoryDto {
    private Integer id;
    private UUID userUuid;
    private Integer amount;
    private Integer balanceSnapshot;
    private DepositHistoryType type;
    private UUID orderItemUuid;
    private LocalDateTime createdAt;
}
