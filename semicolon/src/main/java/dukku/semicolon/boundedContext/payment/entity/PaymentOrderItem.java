package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.shared.payment.dto.PaymentOrderItemDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * 결제 주문 상품 엔티티 (스냅샷)
 * 
 * <p>
 * 결제 시점의 상품명, 가격, 옵션 정보 등을 그대로 보존한다.
 * 원본 상품 정보가 변경되거나 삭제되어도, 결제 당시의 정보를 증빙하기 위해 사용된다.
 */
@Entity
@Table(name = "payment_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PaymentOrderItem extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, comment = "쿠폰 할인액 스냅샷")
    private Integer paymentCoupon;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "판매자 UUID")
    private UUID sellerUuid;

    @Column(nullable = false, comment = "원본 상품 ID 스냅샷")
    private Integer productId;

    @Column(nullable = false, comment = "상품명 스냅샷")
    private String productName;

    @Column(nullable = false, comment = "결제 시점 단가")
    private Integer price;

    // === 정적 팩토리 메서드 ===

    public static PaymentOrderItem create(Payment payment, Integer productId, String productName,
            Integer price, Integer paymentCoupon, UUID sellerUuid) {
        return PaymentOrderItem.builder()
                .payment(payment)
                .productId(productId)
                .productName(productName)
                .price(price)
                .paymentCoupon(paymentCoupon)
                .sellerUuid(sellerUuid)
                .build();
    }

    // === DTO 변환 ===

    public PaymentOrderItemDto toDto() {
        return PaymentOrderItemDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .productId(this.productId)
                .productName(this.productName)
                .price(this.price)
                .paymentCoupon(this.paymentCoupon)
                .sellerUuid(this.sellerUuid)
                .build();
    }
}
