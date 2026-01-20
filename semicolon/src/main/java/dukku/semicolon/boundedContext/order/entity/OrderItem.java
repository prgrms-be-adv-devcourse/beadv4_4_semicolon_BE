package dukku.semicolon.boundedContext.order.entity;

import dukku.common.global.exception.ConflictException;
import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.shared.order.dto.DeliveryInfoRequest;
import dukku.semicolon.shared.order.dto.OrderCreateRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseIdAndUUIDAndTime {
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false)
    private UUID productUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false)
    private UUID sellerUuid;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private int productPrice;

    private String imageUrl;

    @Column(length = 50, comment = "택배사 이름")
    private String carrierName;

    @Column(length = 20, comment = "택배사 코드")
    private String carrierCode;

    @Column(length = 50, comment = "운송장 번호")
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    public static OrderItem createOrderItem(OrderCreateRequest.OrderItemCreateRequest request) {
        return OrderItem.builder()
                .productUuid(request.getProductUuid())
                .sellerUuid(request.getSellerUuid())
                .productName(request.getProductName())
                .productPrice(request.getProductPrice())
                .imageUrl(request.getImageUrl())
                .build();
    }

    public void updateDeliveryInfo(DeliveryInfoRequest request) {
        this.carrierName = request.getCarrierName();
        this.carrierCode = request.getCarrierCode();
        this.trackingNumber = request.getTrackingNumber();
        this.status = OrderItemStatus.SHIPPED;
    }

    public void updateOrderStatus(OrderItemStatus newStatus) {
        if (this.status == newStatus) return;

        validateStateTransition(newStatus);
        this.status = newStatus;
    }

    private void validateStateTransition(OrderItemStatus newStatus) {
        switch (newStatus) {
            case CANCELED -> {
                // 이미 배송 중이거나 배송 완료된 상품은 취소 불가 (반품 절차 밟아야 함)
                if (isShippingOrCompleted()) {
                    throw new ConflictException("이미 배송이 시작되었거나 완료된 상품은 취소할 수 없습니다. 반품을 이용해주세요.");
                }
                // 구매 확정된 상품 취소 불가
                if (this.status == OrderItemStatus.CONFIRMED) {
                    throw new ConflictException("이미 구매 확정된 상품은 취소할 수 없습니다.");
                }
            }
            case CONFIRMED -> {
                // 배송 완료 상태가 아니면 구매 확정 불가
                if (this.status != OrderItemStatus.DELIVERED) {
                    throw new ConflictException("배송이 완료된 상품만 구매 확정할 수 있습니다.");
                }
            }
            // TODO: 환불 정책
            /*case REFUND_REQUESTED -> {
                // 구매 확정 전에는 환불(반품) 요청 가능하지만, 아예 배송도 안 된 거면 취소를 해야 함
                if (!isShippingOrCompleted()) {
                    throw new ConflictException("아직 배송되지 않은 상품입니다. 주문 취소를 이용해주세요.");
                }
                if (this.status == OrderItemStatus.PURCHASE_CONFIRMED) {
                    // 정책에 따라 다름: 구매 확정 후에도 환불 가능한지? 보통은 불가.
                    throw new ConflictException("구매 확정 후에는 반품/환불 신청이 불가능합니다.");
                }
            }*/
            // 그 외 관리자용 상태 변경(배송중 등)은 허용하거나 별도 로직 추가
        }
    }

    private boolean isShippingOrCompleted() {
        return this.status == OrderItemStatus.SHIPPED ||
                this.status == OrderItemStatus.DELIVERED;
    }
}
