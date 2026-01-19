package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 주문 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderDto {
    private Integer id;
    private UUID uuid;
    private UUID userUuid;
    private PaymentOrderStatus status;
    private List<PaymentDto> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
