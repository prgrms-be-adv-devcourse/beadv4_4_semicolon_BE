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

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "주문 UUID (식별용)")
    private UUID orderUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "주문 상품 UUID (식별용)")
    private UUID orderItemUuid;

    @Column(nullable = false, comment = "쿠폰 할인액 스냅샷")
    private Long paymentCoupon;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "판매자 UUID")
    private UUID sellerUuid;

    @Column(nullable = false, comment = "원본 상품 ID 스냅샷")
    private Integer productId;

    @Column(nullable = false, comment = "상품명 스냅샷")
    private String productName;

    @Column(nullable = false, comment = "결제 시점 단가")
    private Long price;

    @Column(nullable = false, comment = "상품별 예치금 사용액 (2026-01-24 추가)")
    private Long paymentDeposit; // 2026-01-24 추가

    /**
     * 결제 주문 상품 스냅샷 생성
     * 
     * @param payment        연관된 결제 엔티티
     * @param orderUuid      주문 식별자
     * @param orderItemUuid  주문 상품 식별자
     * @param productId      원본 상품 ID
     * @param productName    상품명
     * @param price          결제 시점 단가
     * @param paymentCoupon  해당 상품에 적용된 쿠폰 할인액
     * @param sellerUuid     판매자 식별자
     * @param paymentDeposit 이 상품 구매에 사용된 예치금액 (분배 알고리즘에 의해 계산된 값)
     * @return 결제 주문 상품 엔티티
     */
    public static PaymentOrderItem create(Payment payment, UUID orderUuid, UUID orderItemUuid, Integer productId,
            String productName, Long price, Long paymentCoupon, UUID sellerUuid, Long paymentDeposit) {
        return PaymentOrderItem.builder()
                .payment(payment)
                .orderUuid(orderUuid)
                .orderItemUuid(orderItemUuid)
                .productId(productId)
                .productName(productName)
                .price(price)
                .paymentCoupon(paymentCoupon)
                .sellerUuid(sellerUuid)
                .paymentDeposit(paymentDeposit) // 2026-01-24 추가
                .build();
    }

    // === DTO 변환 ===

    public PaymentOrderItemDto toDto() {
        return PaymentOrderItemDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .orderUuid(this.orderUuid)
                .orderItemUuid(this.orderItemUuid)
                .productId(this.productId)
                .productName(this.productName)
                .price(this.price)
                .paymentCoupon(this.paymentCoupon)
                .sellerUuid(this.sellerUuid)
                .paymentDeposit(this.paymentDeposit) // 2026-01-24 추가
                .build();
    }
}
