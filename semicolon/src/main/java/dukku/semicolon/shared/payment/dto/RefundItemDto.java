package dukku.semicolon.shared.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 환불 상품 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundItemDto {
    private Integer id;
    private UUID uuid;
    private Integer paymentOrderItemId;
    private Integer refundAmount;
    private Integer refundDeposit;
    private Integer refundAmountPg;
}
