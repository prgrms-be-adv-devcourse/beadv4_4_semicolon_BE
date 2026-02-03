package dukku.semicolon.shared.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SellerOrderItemResponse {
    private UUID orderItemUuid;      // 판매 관리의 핵심 ID
    private UUID orderUuid;          // 어떤 주문에 속해있는지

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate; // 주문 일시 (Order에서 가져옴)

    private String productName;
    private int productPrice;
    private int quantity;            // (Entity에 수량 필드가 있다면 추가)
    private String imageUrl;

    private OrderItemStatus status;  // 배송 상태 (배송중, 배송완료 등)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt; // 정산용 구매 확정일

    public static SellerOrderItemResponse from(OrderItem item) {
        return SellerOrderItemResponse.builder()
                .orderItemUuid(item.getUuid())
                .orderUuid(item.getOrder().getUuid()) // 지연 로딩 주의 (Repository에서 fetch join 필요)
                .orderDate(item.getOrder().getCreatedAt())
                .productName(item.getProductName())
                .productPrice(item.getProductPrice())
                .imageUrl(item.getImageUrl())
                .status(item.getStatus())
                .confirmedAt(item.getConfirmedAt())
                .build();
    }
}
