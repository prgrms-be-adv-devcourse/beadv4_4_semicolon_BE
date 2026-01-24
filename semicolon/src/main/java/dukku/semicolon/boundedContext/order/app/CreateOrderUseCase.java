package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.order.event.OrderProductSaleBlockedEvent;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.shared.order.dto.OrderCreateRequest;
import dukku.semicolon.shared.product.dto.product.ProductDetailResponse;
import dukku.semicolon.shared.product.out.ProductApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final OrderSupport orderSupport;
    private final ProductApiClient productApiClient;

    @Transactional
    public Order execute(OrderCreateRequest req) {
        Order order = Order.createOrder(req, UserUtil.getUserId());

        req.getItems().stream()
                .map(OrderItem::createOrderItem)
                .forEach(order::addOrderItem);

        Order savedOrder = orderSupport.save(order);

        // 상품 서비스로 해당 상품들이 실제로 존재하는지와 예약 중으로 변경하게 이벤트 전달 (SYNC)
        List<UUID> productUuids = savedOrder.getOrderItems()
                .stream().map(BaseIdAndUUIDAndTime::getUuid).toList();
        productApiClient.reserveProducts(order.getUuid(), productUuids);

        return savedOrder;
    }
}