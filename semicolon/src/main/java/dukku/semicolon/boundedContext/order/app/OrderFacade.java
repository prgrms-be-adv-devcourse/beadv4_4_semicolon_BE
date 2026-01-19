package dukku.semicolon.boundedContext.order.app;

import dukku.common.shared.order.type.OrderItemStatus;
import dukku.common.shared.order.type.OrderStatus;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.shared.order.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFacade {
    private final CreateOrderUseCase createOrder;
    private final FindOrderUseCase findOrder;
    private final UpdateShippingInfoUseCase updateShippingInfo;
    private final FindAdminOrderListUseCase findAdminOrderList;
    private final FindMyOrderListUseCase findMyOrderList;
    private final UpdateOrderItemDeliveryInfoUseCase updateOrderItemDeliveryInfo;
    private final UpdateOrderItemStatusUseCase  updateOrderItemStatus;

    public OrderResponse createOrder(OrderCreateRequest req) {
        return Order.toOrderResponse(createOrder.execute(req));
    }

    // 관리자 또는 사용자가 주문 상세 조회 시
    @Transactional(readOnly = true)
    public OrderResponse findOrderByUuid(UUID orderUuid) {
        return Order.toOrderResponse(findOrder.execute(orderUuid));
    }

    // 사용자가 배송지 정보를 수정하고 싶을 때
    public void updateShippingInfo(UUID orderUuid, OrderUpdateRequest.ShippingInfo req) {
        updateShippingInfo.execute(orderUuid, req);
    }

    // 사용자가 주문내역을 조회하고 싶을 때
    @Transactional(readOnly = true)
    public Page<OrderListResponse> findAdminOrderList(AdminOrderSearchCondition condition, Pageable pageable) {
        return findAdminOrderList.execute(condition, pageable);
    }

    // 사용자가 본인의 주문내역을 조회하고 싶을 때
    @Transactional(readOnly = true)
    public Page<OrderListResponse> findMyOrderList(Pageable pageable) {
        return findMyOrderList.execute(pageable);
    }

    // 판매자가 해당 주문 상품에 운송장을 입력할 경우
    public void updateDeliveryInfo(UUID orderItemUuid, DeliveryInfoRequest request) {
        updateOrderItemDeliveryInfo.execute(orderItemUuid, request);
    }

    // 사용자가 요청하는 경우 또는 관리자가 요청하는 경우. 구매 확정 완료, 취소 요청, 환불 요청
    public void updateDeliveryInfo(UUID orderItemUuid, OrderItemStatus status) {
        updateOrderItemStatus.execute(orderItemUuid, status);
    }
}
