package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 환불 응답 DTO
 *
 * <p>
 * 환불 처리 결과 반환
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundResponse {

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
    private RefundData data;

    /**
     * 환불 결과 데이터
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundData {

        /**
         * 환불 UUID
         */
        private UUID refundId;

        /**
         * 결제 UUID
         */
        private UUID paymentId;

        /**
         * 주문 UUID
         */
        private UUID orderUuid;

        /**
         * 환불 상태
         */
        private PaymentStatus status;

        /**
         * 환불 금액 정보
         */
        private RefundAmountInfo amounts;

        /**
         * PG 정보
         */
        private PgInfo pg;

        /**
         * 환불 요청 생성 시각
         */
        private OffsetDateTime createdAt;

        /**
         * 환불 완료 시각
         */
        private OffsetDateTime completedAt;
    }

    /**
     * 환불 금액 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundAmountInfo {

        /**
         * 요청된 환불 금액
         */
        private Integer requestedRefundAmount;

        /**
         * 예치금 환불 금액
         */
        private Integer depositRefundAmount;

        /**
         * PG 환불 금액
         */
        private Integer pgRefundAmount;
    }

    /**
     * PG 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PgInfo {

        /**
         * PG사 이름
         */
        private String provider;

        /**
         * 토스 주문 ID
         */
        private String tossOrderId;

        /**
         * 토스 취소 트랜잭션 키
         */
        private String cancelTransactionKey;
    }
}
