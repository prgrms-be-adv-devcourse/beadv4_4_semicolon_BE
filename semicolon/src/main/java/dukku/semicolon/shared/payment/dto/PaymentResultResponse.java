package dukku.semicolon.shared.payment.dto;

import dukku.common.shared.payment.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제 내역 상세 조회 응답 DTO
 *
 * <p>
 * 결제 상세 정보 조회 시 반환
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultResponse {

    private boolean success;
    private String code;
    private String message;
    private PaymentResultData data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentResultData {
        private UUID paymentUuid;
        private UUID orderUuid;
        private String tossOrderId;
        private PaymentStatus status;

        // 금액 정보
        private Long totalAmount;
        private Long couponDiscountAmount;
        private Long depositUseAmount;
        private Long pgPayAmount;
        private Long refundTotal;

        // 일시 정보
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;

        // 상세 내역
        private List<PaymentOrderItemDto> items;
        private List<RefundDto> refunds;
    }
}
