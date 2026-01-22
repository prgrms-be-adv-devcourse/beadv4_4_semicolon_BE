package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 환불 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRefundResponse {

    private boolean success;
    private String code;
    private String message;
    private RefundData data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundData {
        private UUID refundId;
        private UUID paymentId;
        private UUID orderUuid;
        private PaymentStatus status;
        private RefundAmountInfo amounts;
        private PgInfo pg;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundAmountInfo {
        private Long requestedRefundAmount;
        private Long depositRefundAmount;
        private Long pgRefundAmount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PgInfo {
        private String provider;
        private String tossOrderId;
        private String cancelTransactionKey;
    }
}
