package dukku.semicolon.shared.payment.dto;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 결제 내역 상세 조회 응답 DTO
 *
 * <p>
 * 결제 상세 정보 조회 시 반환
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultResponse {

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
    private PaymentResultData data;

    /**
     * 결제 조회 결과 데이터
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentResultData {

        /**
         * 결제 UUID
         */
        private UUID paymentId;

        /**
         * 주문 UUID
         */
        private UUID orderUuid;

        /**
         * 결제 상태
         */
        private PaymentStatus status;

        /**
         * 금액 정보
         */
        private AmountInfo amounts;

        /**
         * 결제 요청 생성 시각
         */
        private OffsetDateTime createdAt;

        /**
         * 승인 일시
         */
        private OffsetDateTime approvedAt;
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
         * 총 결제 금액
         */
        private Integer totalAmount;

        /**
         * 쿠폰 할인 금액
         */
        private Integer couponDiscountAmount;

        /**
         * 예치금 사용 금액
         */
        private Integer depositUseAmount;

        /**
         * PG 결제 금액
         */
        private Integer pgPayAmount;

        /**
         * 최종 결제 금액
         */
        private Integer finalPayAmount;
    }
}
