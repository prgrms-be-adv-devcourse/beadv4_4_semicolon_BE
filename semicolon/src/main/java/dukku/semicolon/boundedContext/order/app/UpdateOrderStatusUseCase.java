package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.order.event.OrderProductSaleConfirmedEvent;
import dukku.common.shared.order.event.OrderProductSaleReleasedEvent;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.common.shared.order.type.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {
    private final OrderSupport orderSupport;
    private final EventPublisher eventPublisher;

    // 1. 결제 완료 처리 (PG창에서 결제 완료 할 경우)
    @Transactional
    public void confirmPayment(UUID orderUuid) {
        Order order = orderSupport.findOrderByUuid(orderUuid);

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            log.warn("이미 처리된 주문입니다. 결제 승인 무시. orderUuid={}", orderUuid);
            return;
        }

        updateOrderItemsStatus(order, OrderItemStatus.PAYMENT_COMPLETED);
        order.updateOrderStatus(OrderStatus.PAID);

        eventPublisher.publish(
                new OrderProductSaleConfirmedEvent(order.getUuid(), order.getProductUuids())
        );
    }

    // 2. 결제 실패 처리 (PG창에서 결제 실패 할 경우)
    @Transactional
    public void failPayment(UUID orderUuid) {
        Order order = orderSupport.findOrderByUuid(orderUuid);

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("유효하지 않은 결제 실패 요청입니다 (이미 처리됨). orderUuid={}, currentStatus={}",
                    orderUuid, order.getStatus());
            return;
        }

        updateOrderItemsStatus(order, OrderItemStatus.CANCELED);
        order.updateOrderStatus(OrderStatus.CANCELED);

        eventPublisher.publish(
                new OrderProductSaleReleasedEvent(order.getUuid(), order.getProductUuids())
        );
    }

    // 내부 공통 메서드 (private)
    private void updateOrderItemsStatus(Order order, OrderItemStatus status) {
        order.getOrderItems().forEach(item -> item.updateOrderStatus(status));
    }
}