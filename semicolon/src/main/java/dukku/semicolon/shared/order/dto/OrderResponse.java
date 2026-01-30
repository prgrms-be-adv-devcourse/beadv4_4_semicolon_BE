package dukku.semicolon.shared.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.common.shared.order.type.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {
    // 1. 주문 기본 정보
    private UUID orderUuid;
    private UUID userUuid;

    // 2. 결제 및 상태 정보
    private int totalAmount;
    private int refundedAmount;
    private OrderStatus orderStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderedAt;

    // 3. 배송지 정보
    private String recipient;
    private String contactNumber;
    private String address;

    private List<OrderItemResponse> items;

    @Getter
    @Builder
    public static class OrderItemResponse {
        private UUID productUuid;
        private String productName;
        private int productPrice;
        private String imageUrl;
        private OrderItemStatus itemStatus;

        private String carrierName;
        private String trackingNumber;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .productUuid(item.getProductUuid())
                    .productName(item.getProductName())
                    .productPrice(item.getProductPrice())
                    .imageUrl(item.getImageUrl())
                    .itemStatus(item.getStatus())
                    .carrierName(item.getCarrierName())
                    .trackingNumber(item.getTrackingNumber())
                    .build();
        }
    }
}