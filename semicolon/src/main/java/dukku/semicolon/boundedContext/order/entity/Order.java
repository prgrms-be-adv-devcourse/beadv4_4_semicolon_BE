package dukku.semicolon.boundedContext.order.entity;

import dukku.common.global.exception.ConflictException;
import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.common.shared.order.type.OrderStatus;
import dukku.semicolon.shared.order.dto.OrderCreateRequest;
import dukku.semicolon.shared.order.dto.OrderResponse;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseIdAndUUIDAndTime {
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, comment = "구매자 UUID")
    private UUID userUuid;

    @Column(nullable = false, comment = "수령 총 금액")
    private int totalAmount;

    @Column(nullable = false, comment = "수령 주소")
    private String address;

    @Column(nullable = false, length = 50, comment = "수령인")
    private String recipient;

    @Column(nullable = false, length = 50, comment = "수령인 연락처")
    private String contactNumber;

    private int refundedAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public static Order createOrder(OrderCreateRequest request, UUID userUuid) {
        int totalAmount = request.getItems().stream()
                .mapToInt(OrderCreateRequest.OrderItemCreateRequest::getProductPrice)
                .sum();

        return Order.builder()
                .userUuid(userUuid)
                .totalAmount(totalAmount)
                .address(request.getAddress())
                .recipient(request.getRecipient())
                .contactNumber(request.getContactNumber())
                .refundedAmount(0)
                .build();
    }

    public void updateOrderStatus(OrderStatus status) {
        this.status = status;
    }

    public void updateOrderForUser(String address, String recipient, String contactNumber) {
        if (!isShipped()) {
            throw new ConflictException("이미 배송 준비 중이거나 완료된 상품이 있어 배송지를 변경할 수 없습니다.");
        }

        this.address = address;
        this.recipient = recipient;
        this.contactNumber = contactNumber;
    }

    public void updateRefundedAmount(int refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderUuid(order.getUuid())
                .userUuid(order.getUserUuid())
                .totalAmount(order.getTotalAmount())
                .refundedAmount(order.getRefundedAmount())
                .orderStatus(order.getStatus())
                .orderedAt(order.getCreatedAt())
                .recipient(order.getRecipient())
                .contactNumber(order.getContactNumber())
                .address(order.getAddress())
                .items(order.getOrderItems().stream()
                        .map(OrderResponse.OrderItemResponse::from)
                        .toList())
                .build();
    }

    public boolean isShipped() {
        return orderItems.stream()
                .map(OrderItem::getStatus)
                .allMatch(OrderItemStatus::canChangeShippingInfo);
    }

    public List<UUID> getProductUuids() {
        return orderItems.stream()
                .map(BaseIdAndUUIDAndTime::getUuid)
                .toList();
    }
}
