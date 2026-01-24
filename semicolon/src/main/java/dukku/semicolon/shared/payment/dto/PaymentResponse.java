package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 요청 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private boolean success;
    private String code;
    private String message;
    private PaymentRequestedData data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentRequestedData {
        /**
         * 결제 UUID
         */
        private UUID paymentUuid;

        /**
         * 결제 상태
         */
        private PaymentStatus status;

        /**
         * 토스 결제 정보
         */
        private TossInfo toss;

        /**
         * 금액 정보
         */
        private ResponseAmounts amounts;

        /**
         * 결제 요청 생성 시각
         */
        private LocalDateTime createdAt;
    }

    /**
     * 토스 결제창 호출에 필요한 정보
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TossInfo {

        /**
         * 토스 주문 ID (백엔드에서 생성)
         */
        private String orderId;

        /**
         * PG 결제 금액
         */
        private Long amount;

        /**
         * 주문명
         */
        private String orderName;

        /**
         * 결제 성공 시 리다이렉트 URL
         */
        private String successUrl;

        /**
         * 결제 실패 시 리다이렉트 URL
         */
        private String failUrl;
    }

    /**
     * 응답 금액 정보
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseAmounts {

        /**
         * 최종 결제 금액
         */
        private Long finalPayAmount;

        /**
         * 예치금 사용 금액
         */
        private Long depositUseAmount;

        /**
         * PG 결제 금액
         */
        private Long pgPayAmount;
    }
}
