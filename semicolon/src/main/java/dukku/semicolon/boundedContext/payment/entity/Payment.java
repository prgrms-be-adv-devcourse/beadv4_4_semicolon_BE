package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.auth.crypto.converter.AesGcmConverter;
import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentType;
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

/**
 * 결제 정보 엔티티
 * 
 * <p>
 * PG사 결제 정보와 예치금 사용 정보를 포함하는 결제 핵심 엔티티.
 * 하나의 PaymentOrder에 여러 Payment가 연결될 수 있으며,
 * 각 Payment는 복수의 PaymentOrderItem(스냅샷)과 Refund를 가질 수 있다.
 * 
 * <h3>금액 필드 설명</h3>
 * <ul>
 * <li>{@code amount} - 주문 총액 (쿠폰 적용 전)</li>
 * <li>{@code paymentCouponTotal} - 쿠폰으로 할인된 총액</li>
 * <li>{@code paymentDepositOrigin} - 결제 체결 시점의 예치금 사용액 (불변)</li>
 * <li>{@code paymentDeposit} - 현재 적용된 예치금 (환불로 변동 가능)</li>
 * <li>{@code amountPgOrigin} - 결제 체결 시점의 PG 승인액 (불변)</li>
 * <li>{@code amountPg} - 현재 PG 승인액 (환불로 변동 가능)</li>
 * <li>{@code refundTotal} - 누적 환불 총액</li>
 * </ul>
 * 
 * <h3>결제 유형</h3>
 * <ul>
 * <li>DEPOSIT - 예치금 전액 결제</li>
 * <li>MIXED - 예치금 + PG 복합 결제</li>
 * </ul>
 * 
 * @see PaymentOrder 결제 주문 (레플리카)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    private PaymentOrder paymentOrder;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "uuid")
    private UUID userUuid;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer amount; // 주문 총액

    @Column(name = "payment_deposit_origin", nullable = false, columnDefinition = "integer default 0")
    private Integer paymentDepositOrigin; // 최초 예치금 사용액

    @Column(name = "payment_deposit", nullable = false, columnDefinition = "integer default 0")
    private Integer paymentDeposit; // 현재 적용 예치금

    @Column(name = "amount_pg_origin", nullable = false, columnDefinition = "integer default 0")
    private Integer amountPgOrigin; // 최초 PG 승인액

    @Column(name = "amount_pg", nullable = false, columnDefinition = "integer default 0")
    private Integer amountPg; // 현재 PG 승인액 (환불로 변동)

    @Column(name = "payment_coupon_total", nullable = false, columnDefinition = "integer default 0")
    private Integer paymentCouponTotal; // 쿠폰 할인 총액

    @Column(name = "refund_total", nullable = false, columnDefinition = "integer default 0")
    private Integer refundTotal; // 환불 총액

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Convert(converter = AesGcmConverter.class)
    @Column(name = "pg_payment_key", length = 50)
    private String pgPaymentKey; // 토스페이먼츠 결제키 (암호화)

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 결제 승인 시점

    @Builder.Default
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentOrderItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds = new ArrayList<>();

    // === 정적 팩토리 메서드 ===

    public static Payment create(PaymentOrder paymentOrder, UUID userUuid, Integer amount,
            Integer depositAmount, Integer pgAmount, Integer couponAmount,
            PaymentType paymentType) {
        return Payment.builder()
                .paymentOrder(paymentOrder)
                .userUuid(userUuid)
                .amount(amount)
                .paymentDepositOrigin(depositAmount)
                .paymentDeposit(depositAmount)
                .amountPgOrigin(pgAmount)
                .amountPg(pgAmount)
                .paymentCouponTotal(couponAmount)
                .refundTotal(0)
                .paymentType(paymentType)
                .paymentStatus(PaymentStatus.PENDING)
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

    public void partialCancel(Integer refundAmount) {
        this.refundTotal = (this.refundTotal == null ? 0 : this.refundTotal) + refundAmount;
        this.paymentStatus = PaymentStatus.PARTIAL_CANCELED;
    }

    public void addRefund(Refund refund) {
        this.refunds.add(refund);
    }

    public void addItem(PaymentOrderItem item) {
        this.items.add(item);
    }

    // === DTO 변환 ===

    public PaymentDto toDto() {
        return PaymentDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .userUuid(this.userUuid)
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
