package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.PaymentStatus;
import dukku.common.shared.payment.type.PaymentType;
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
    private UUID orderUuid;
    private UUID userUuid;
    private Long amount;
    private Long paymentDeposit;
    private Long amountPg;
    private Long paymentCouponTotal;
    private Long refundTotal;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<PaymentOrderItemDto> items;
}
