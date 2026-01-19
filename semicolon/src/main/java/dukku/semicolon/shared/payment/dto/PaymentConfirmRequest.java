package dukku.semicolon.shared.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 결제 승인 확정 요청 DTO
 *
 * <p>
 * 토스페이먼츠 인증 후 백엔드에 승인을 요청할 때 사용
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequest {

    /**
     * 내부 결제 UUID
     */
    @NotNull(message = "paymentUuid는 필수입니다.")
    private UUID paymentUuid;

    /**
     * 토스 결제 정보
     */
    @NotNull(message = "toss 정보는 필수입니다.")
    @Valid
    private TossConfirmInfo toss;

    /**
     * 토스 결제 승인에 필요한 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TossConfirmInfo {

        /**
         * 토스 결제 키
         */
        @NotBlank(message = "paymentKey는 필수입니다.")
        private String paymentKey;

        /**
         * 토스 주문 ID
         */
        @NotBlank(message = "orderId는 필수입니다.")
        private String orderId;

        /**
         * PG 결제 금액
         */
        @NotNull(message = "amount는 필수입니다.")
        private Integer amount;
    }
}
