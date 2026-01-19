package dukku.semicolon.shared.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 환불 요청 DTO
 *
 * <p>
 * 결제 취소/환불 요청 시 사용.
 * items가 있으면 부분 환불, 없으면 전체 환불로 처리.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundRequest {

    /**
     * 결제 UUID
     */
    @NotNull(message = "결제 UUID는 필수입니다.")
    private UUID paymentId;

    /**
     * 주문 UUID
     */
    @NotNull(message = "주문 UUID는 필수입니다.")
    private UUID orderUuid;

    /**
     * 총 환불 금액
     */
    @NotNull(message = "환불 금액은 필수입니다.")
    @Min(value = 1, message = "환불 금액은 1원 이상이어야 합니다.")
    private Integer refundAmount;

    /**
     * 환불 사유
     */
    @NotBlank(message = "환불 사유는 필수입니다.")
    private String reason;

    /**
     * 환불 대상 상품 목록 (부분 환불 시 사용)
     */
    @Valid
    private List<RefundItemInfo> items;

    /**
     * 환불 대상 상품 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundItemInfo {

        /**
         * 주문 상품 UUID
         */
        @NotNull(message = "주문 상품 UUID는 필수입니다.")
        private UUID orderItemUuid;

        /**
         * 개별 상품 환불 금액
         */
        @NotNull(message = "상품별 환불 금액은 필수입니다.")
        @Min(value = 1, message = "환불 금액은 1원 이상이어야 합니다.")
        private Integer refundAmount;
    }
}
