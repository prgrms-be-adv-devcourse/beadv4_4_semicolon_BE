package dukku.semicolon.boundedContext.order.in;

import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.app.OrderFacade;
import dukku.semicolon.shared.order.dto.*;
import dukku.semicolon.shared.order.docs.OrderApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@OrderApiDocs.OrderTag
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    @OrderApiDocs.CreateOrder
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest req) {
        OrderResponse response = orderFacade.createOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderUuid}")
    @OrderApiDocs.FindOrderByUuid
    public ResponseEntity<OrderResponse> findOrderByUuid(@PathVariable UUID orderUuid) {
        OrderResponse response = orderFacade.findOrderByUuid(orderUuid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderUuid}/shipping-info")
    @OrderApiDocs.UpdateShippingInfo
    public ResponseEntity<Void> updateShippingInfo(
            @PathVariable UUID orderUuid,
            @RequestBody @Validated OrderUpdateRequest.ShippingInfo req
    ) {
        orderFacade.updateShippingInfo(orderUuid, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @OrderApiDocs.FindAdminOrderList
    public ResponseEntity<Page<OrderListResponse>> findAdminOrderList(AdminOrderSearchCondition condition, Pageable pageable) {
        Page<OrderListResponse> response = orderFacade.findAdminOrderList(condition, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @OrderApiDocs.FindMyOrderList
    public ResponseEntity<Page<OrderListResponse>> findMyOrderList(Pageable pageable) {
        Page<OrderListResponse> response = orderFacade.findMyOrderList(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{orderItemUuid}/delivery-info")
    @OrderApiDocs.UpdateOrderItemDeliveryInfo
    public ResponseEntity<Void> updateDeliveryInfo(
            @PathVariable UUID orderItemUuid,
            @RequestBody @Validated DeliveryInfoRequest request
    ) {
        orderFacade.updateDeliveryInfo(orderItemUuid, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{orderItemUuid}/status")
    @OrderApiDocs.UpdateOrderItemStatus
    public ResponseEntity<Void> updateOrderItemStatus(@PathVariable UUID orderItemUuid, @RequestParam OrderItemStatus status) {
        orderFacade.updateDeliveryInfo(orderItemUuid, status);
        return ResponseEntity.noContent().build();
    }
}