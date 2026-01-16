package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.shared.payment.dto.RefundItemDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 환불 상품 엔티티
 * 
 * <p>
 * 부분 환불 시 어떤 상품이 얼마만큼 환불되었는지 기록한다.
 */
@Entity
@Table(name = "refund_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class RefundItem extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_item_id", nullable = false)
    private PaymentOrderItem paymentOrderItem;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount; // 결제주문상품 금액 - 쿠폰 할인액

    @Column(name = "refund_deposit", nullable = false)
    private Integer refundDeposit; // 이 상품에 적용된 환불 예치금 금액

    @Column(name = "refund_amount_pg", nullable = false)
    private Integer refundAmountPg; // 환불대상액 - 환불 예치금

    // === 정적 팩토리 메서드 ===

    public static RefundItem create(Refund refund, PaymentOrderItem item,
            Integer amount, Integer deposit, Integer amountPg) {
        return RefundItem.builder()
                .refund(refund)
                .paymentOrderItem(item)
                .refundAmount(amount)
                .refundDeposit(deposit)
                .refundAmountPg(amountPg)
                .build();
    }

    // === DTO 변환 ===

    public RefundItemDto toDto() {
        return RefundItemDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .paymentOrderItemId(this.paymentOrderItem.getId())
                .refundAmount(this.refundAmount)
                .refundDeposit(this.refundDeposit)
                .refundAmountPg(this.refundAmountPg)
                .build();
    }
}
