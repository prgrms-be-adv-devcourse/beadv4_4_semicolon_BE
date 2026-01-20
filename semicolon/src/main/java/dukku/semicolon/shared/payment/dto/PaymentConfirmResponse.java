package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 결제 승인 확정 응답 DTO
 *
 * <p>
 * 토스 결제 승인 완료 후 프론트에 반환하는 데이터
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmResponse {

    /**
     * 처리 성공 여부
     */
    private boolean success;

    /**
     * 결과 코드
     */
    private String code;

    /**
     * 결과 메시지
     */
    private String message;

    /**
     * 응답 데이터
     */
    private PaymentConfirmData data;

    /**
     * 결제 승인 결과 데이터
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentConfirmData {

        /**
         * 결제 UUID
         */
        private UUID paymentUuid;

        /**
         * 결제 상태
         */
        private PaymentStatus status;

        /**
         * 승인 일시
         */
        private OffsetDateTime approvedAt;

        /**
         * 토스 정보
         */
        private TossInfo toss;

        /**
         * 금액 정보
         */
        private AmountInfo amounts;
    }

    /**
     * 토스 결제 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TossInfo {

        /**
         * 토스 주문 ID
         */
        private String orderId;

        /**
         * 토스 결제 키
         */
        private String paymentKey;
    }

    /**
     * 금액 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmountInfo {

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
