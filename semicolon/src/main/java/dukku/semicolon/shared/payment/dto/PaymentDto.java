package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Integer id;
    private UUID uuid;
    private UUID userUuid;
    private Integer amount;
    private Integer paymentDeposit;
    private Integer amountPg;
    private Integer paymentCouponTotal;
    private Integer refundTotal;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<PaymentOrderItemDto> items;
}
