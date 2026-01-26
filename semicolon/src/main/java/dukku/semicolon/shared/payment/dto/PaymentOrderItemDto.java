package dukku.semicolon.shared.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 결제 주문 상품 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderItemDto {
    private Integer id;
    private UUID uuid;
    private UUID orderUuid;
    private UUID orderItemUuid;
    private UUID sellerUuid;
    private Integer productId;
    private String productName;
    private Long price;
    private Long paymentCoupon;
    private Long paymentDeposit; // 2026-01-24 추가
}
