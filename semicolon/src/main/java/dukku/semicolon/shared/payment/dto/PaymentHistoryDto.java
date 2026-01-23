package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 이력 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDto {
    private Integer id;
    private UUID uuid;
    private Integer paymentId;
    private PaymentHistoryType type;
    private PaymentStatus paymentStatusOrigin;
    private PaymentStatus paymentStatusChanged;
    private Long amountPgOrigin;
    private Long amountPgChanged;
    private Long paymentDepositOrigin;
    private Long paymentDepositChanged;
    private LocalDateTime createdAt;
}
