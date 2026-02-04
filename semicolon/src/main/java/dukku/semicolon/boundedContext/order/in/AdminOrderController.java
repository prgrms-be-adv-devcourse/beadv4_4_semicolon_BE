package dukku.semicolon.boundedContext.order.in;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.semicolon.boundedContext.order.app.facade.AdminOrderFacade;
import dukku.semicolon.shared.order.docs.AdminOrderApiDocs;
import dukku.semicolon.shared.order.dto.AdminOrderSearchCondition;
import dukku.semicolon.shared.order.dto.OrderListResponse;
import dukku.semicolon.shared.order.dto.SellerOrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
@AdminOrderApiDocs.AdminOrderTag
public class AdminOrderController {

    private final AdminOrderFacade adminOrderFacade;

    // 1. 전체 주문 목록 조회
    @AdminOrderApiDocs.FindAllOrders
    @GetMapping
    public ResponseEntity<Page<OrderListResponse>> findAllOrders(
            @ModelAttribute AdminOrderSearchCondition condition,
            @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminOrderFacade.findAllOrders(condition, pageable));
    }

    // 2. 특정 판매자 판매 내역 조회
    @AdminOrderApiDocs.FindSellerSalesHistory
    @GetMapping("/sellers/{sellerUuid}")
    public ResponseEntity<Page<SellerOrderItemResponse>> findSellerSalesHistory(
            @PathVariable UUID sellerUuid,
            @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminOrderFacade.findSellerSalesHistory(sellerUuid, pageable));
    }

    // 3. 정산용 구매 확정 목록 조회
    @AdminOrderApiDocs.FindConfirmedItems
    @GetMapping("/confirmed-items")
    public ResponseEntity<List<ConfirmedOrderItemResponse>> findConfirmedItems(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        return ResponseEntity.ok(adminOrderFacade.findConfirmedItems(start, end));
    }
}
