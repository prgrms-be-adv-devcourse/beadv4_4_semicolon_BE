package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 승인 확정 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmResponse {

    private boolean success;
    private String code;
    private String message;
    private PaymentConfirmData data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentConfirmData {
        private UUID paymentUuid;
        private PaymentStatus status;
        private LocalDateTime approvedAt;
        private TossInfo toss;
        private AmountInfo amounts;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TossInfo {
        private String orderId;
        private String paymentKey;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmountInfo {
        private Long finalPayAmount;
        private Long depositUseAmount;
        private Long pgPayAmount;
    }
}
