package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 환불 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDto {
    private Integer id;
    private UUID uuid;
    private Long refundAmountTotal;
    private Long refundDepositTotal;
    private RefundStatus refundStatus;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<RefundItemDto> items;
}
