package dukku.semicolon.shared.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 결제 요청 DTO
 *
 * <p>
 * 프론트에서 결제 준비(prepare) 요청 시 전송하는 데이터
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    /**
     * 주문 UUID
     */
    @NotNull(message = "주문 UUID는 필수입니다.")
    private UUID orderUuid;

    /**
     * 쿠폰 UUID (선택)
     */
    private UUID couponUuid;

    /**
     * 금액 정보
     */
    @NotNull(message = "금액 정보는 필수입니다.")
    @Valid
    private Amounts amounts;

    /**
     * 주문명 (토스 결제창에 표시)
     */
    @NotBlank(message = "주문명은 필수입니다.")
    @Size(max = 100, message = "주문명은 100자 이하여야 합니다.")
    private String orderName;

    /**
     * 결제 요청 금액 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Amounts {

        /**
         * 상품 총액 (상품 가격 합계)
         */
        @NotNull(message = "상품 총액은 필수입니다.")
        @Min(value = 0, message = "상품 총액은 0 이상이어야 합니다.")
        private Long itemsTotalAmount;

        /**
         * 쿠폰 할인 금액
         */
        @NotNull(message = "쿠폰 할인 금액은 필수입니다.")
        @Min(value = 0, message = "쿠폰 할인 금액은 0 이상이어야 합니다.")
        private Long couponDiscountAmount;

        /**
         * 최종 결제 금액 (상품 총액 - 쿠폰 할인)
         */
        @NotNull(message = "최종 결제 금액은 필수입니다.")
        @Min(value = 0, message = "최종 결제 금액은 0 이상이어야 합니다.")
        private Long finalPayAmount;

        /**
         * 예치금 사용 금액
         */
        @NotNull(message = "예치금 사용 금액은 필수입니다.")
        @Min(value = 0, message = "예치금 사용 금액은 0 이상이어야 합니다.")
        private Long depositUseAmount;

        /**
         * PG 결제 금액 (최종 결제 금액 - 예치금 사용)
         */
        @NotNull(message = "PG 결제 금액은 필수입니다.")
        @Min(value = 0, message = "PG 결제 금액은 0 이상이어야 합니다.")
        private Long pgPayAmount;
    }
}
