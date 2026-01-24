package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.auth.crypto.converter.AesGcmConverter;
import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.common.shared.payment.type.PaymentType;
import dukku.semicolon.shared.payment.dto.PaymentResponse;
import dukku.semicolon.shared.payment.dto.PaymentConfirmResponse;
import dukku.semicolon.shared.payment.dto.PaymentResultResponse;
import dukku.semicolon.shared.payment.dto.PaymentOrderItemDto;
import dukku.semicolon.shared.payment.dto.RefundDto;
import dukku.semicolon.shared.payment.dto.PaymentDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 결제 정보 엔티티
 * 
 * <p>
 * PG사 결제 정보와 예치금 사용 정보를 포함하는 결제 핵심 엔티티.
 * 각 Payment는 복수의 PaymentOrderItem(스냅샷)과 Refund를 가질 수 있다.
 * 
 * @see PaymentOrderItem 결제 주문 상품 (스냅샷)
 * @see Refund 환불 정보
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseIdAndUUIDAndTime {

        @JdbcTypeCode(SqlTypes.UUID)
        @Column(nullable = false, columnDefinition = "uuid", comment = "주문 UUID")
        private UUID orderUuid;

        @JdbcTypeCode(SqlTypes.UUID)
        @Column(nullable = false, columnDefinition = "uuid", comment = "결제 유저 UUID")
        private UUID userUuid;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "주문 총액")
        private Long amount;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "최초 예치금 사용액 (결제 체결 시점 원본)")
        private Long paymentDepositOrigin;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "현재 적용된 예치금 (환불로 변동 가능)")
        private Long paymentDeposit;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "현재 PG 승인액 (환불로 변동 가능)")
        private Long amountPg;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "최초 PG 승인액 (결제 체결 시점 원본)")
        private Long amountPgOrigin;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "쿠폰 총 할인액")
        private Long paymentCouponTotal;

        @Column(nullable = false, columnDefinition = "bigint default 0", comment = "누적 환불 총액")
        private Long refundTotal;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, comment = "결제 수단 (DEPOSIT, MIXED)")
        private PaymentType paymentType;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, comment = "결제 상태")
        private PaymentStatus paymentStatus;

        @Convert(converter = AesGcmConverter.class)
        @Column(length = 255, comment = "토스페이먼츠 결제키 (암호화)")
        private String pgPaymentKey;

        @Column(comment = "결제 승인 시점")
        private LocalDateTime approvedAt;

        @Column(comment = "토스 주문 ID")
        private String tossOrderId;

        @Builder.Default
        @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<PaymentOrderItem> items = new ArrayList<>();

        @Builder.Default
        @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Refund> refunds = new ArrayList<>();

        // === 정적 팩토리 메서드 ===

        public static Payment create(UUID orderUuid, UUID userUuid, Long amount,
                        Long depositAmount, Long pgAmount, Long couponAmount,
                        PaymentType paymentType, String tossOrderId) {
                return Payment.builder()
                                .orderUuid(orderUuid)
                                .userUuid(userUuid)
                                .amount(amount)
                                .paymentDepositOrigin(depositAmount)
                                .paymentDeposit(depositAmount)
                                .amountPgOrigin(pgAmount)
                                .amountPg(pgAmount)
                                .paymentCouponTotal(couponAmount)
                                .refundTotal(0L)
                                .paymentType(paymentType)
                                .paymentStatus(PaymentStatus.PENDING)
                                .tossOrderId(tossOrderId)
                                .build();
        }

        // === 도메인 로직 ===

        public void approve(String pgPaymentKey) {
                this.pgPaymentKey = pgPaymentKey;
                this.paymentStatus = PaymentStatus.DONE;
                this.approvedAt = LocalDateTime.now();
        }

        public void fail() {
                this.paymentStatus = PaymentStatus.FAILED;
        }

        public void cancel() {
                this.paymentStatus = PaymentStatus.CANCELED;
        }

        public void partialCancel(Long refundAmount) {
                this.refundTotal = (this.refundTotal == null ? 0L : this.refundTotal) + refundAmount;
                this.paymentStatus = PaymentStatus.PARTIAL_CANCELED;
        }

        public void rollbackStatus() {
                this.paymentStatus = PaymentStatus.FAILED;
        }

        public void rollbackFailedStatus() {
                this.paymentStatus = PaymentStatus.ROLLBACK_FAILED;
        }

        public void addRefund(Refund refund) {
                this.refunds.add(refund);
        }

        public void addItem(PaymentOrderItem item) {
                this.items.add(item);
        }

        // === DTO 변환 ===

        public PaymentResponse toPaymentResponse(String orderName) {
                return PaymentResponse.builder()
                                .success(true)
                                .code("PAYMENT_REQUESTED")
                                .message("결제 요청이 생성되었습니다.")
                                .data(PaymentResponse.PaymentRequestedData.builder()
                                                .paymentUuid(this.getUuid())
                                                .status(this.paymentStatus)
                                                .toss(PaymentResponse.TossInfo.builder()
                                                                .orderId(this.tossOrderId)
                                                                .amount(this.amountPg)
                                                                .orderName(orderName)
                                                                .successUrl("https://localhost:3000/payments/success?paymentUuid="
                                                                                + this.getUuid())
                                                                .failUrl("https://localhost:3000/payments/fail?paymentUuid="
                                                                                + this.getUuid())
                                                                .build())
                                                .amounts(PaymentResponse.ResponseAmounts.builder()
                                                                .finalPayAmount(this.amount)
                                                                .depositUseAmount(this.paymentDeposit)
                                                                .pgPayAmount(this.amountPg)
                                                                .build())
                                                .createdAt(this.getCreatedAt())
                                                .build())
                                .build();
        }

        public PaymentConfirmResponse toPaymentConfirmResponse(boolean success, String message) {
                return PaymentConfirmResponse.builder()
                                .success(success)
                                .code(success ? "PAYMENT_CONFIRMED" : "PAYMENT_CONFIRM_FAILED")
                                .message(message)
                                .data(PaymentConfirmResponse.PaymentConfirmData.builder()
                                                .paymentUuid(this.getUuid())
                                                .status(this.paymentStatus)
                                                .approvedAt(this.approvedAt)
                                                .toss(PaymentConfirmResponse.TossInfo.builder()
                                                                .orderId(this.tossOrderId)
                                                                .paymentKey(this.pgPaymentKey)
                                                                .build())
                                                .amounts(PaymentConfirmResponse.AmountInfo.builder()
                                                                .finalPayAmount(this.amount)
                                                                .depositUseAmount(this.paymentDeposit)
                                                                .pgPayAmount(this.amountPg)
                                                                .build())
                                                .build())
                                .build();
        }

        public PaymentResultResponse toPaymentResultResponse() {
                return PaymentResultResponse.builder()
                                .success(true)
                                .code("PAYMENT_RESULT_RETRIEVED")
                                .message("결제 내역을 조회했습니다.")
                                .data(PaymentResultResponse.PaymentResultData.builder()
                                                .paymentUuid(this.getUuid())
                                                .orderUuid(this.orderUuid)
                                                .tossOrderId(this.tossOrderId)
                                                .status(this.paymentStatus)
                                                .totalAmount(this.amount)
                                                .couponDiscountAmount(this.paymentCouponTotal)
                                                .depositUseAmount(this.paymentDeposit)
                                                .pgPayAmount(this.amountPg)
                                                .refundTotal(this.refundTotal)
                                                .createdAt(this.getCreatedAt())
                                                .approvedAt(this.approvedAt)
                                                .items(this.items.stream()
                                                                .map(PaymentOrderItem::toDto)
                                                                .collect(Collectors.toList()))
                                                .refunds(this.refunds.stream()
                                                                .map(Refund::toDto)
                                                                .collect(Collectors.toList()))
                                                .build())
                                .build();
        }

        public PaymentDto toDto() {
                return PaymentDto.builder()
                                .id(this.getId())
                                .uuid(this.getUuid())
                                .orderUuid(this.orderUuid)
                                .userUuid(this.userUuid)
                                .tossOrderId(this.tossOrderId)
                                .amount(this.amount)
                                .paymentDeposit(this.paymentDeposit)
                                .amountPg(this.amountPg)
                                .paymentCouponTotal(this.paymentCouponTotal)
                                .refundTotal(this.refundTotal)
                                .paymentType(this.paymentType)
                                .paymentStatus(this.paymentStatus)
                                .approvedAt(this.approvedAt)
                                .createdAt(this.getCreatedAt())
                                .items(this.items.stream().map(PaymentOrderItem::toDto).toList())
                                .build();
        }
}
