package dukku.semicolon.boundedContext.order.entity;

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

    public void updateOrderStatus(OrderItemStatus status) {
        this.status = status;
    }
}
