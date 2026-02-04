package dukku.semicolon.boundedContext.order.app.facade;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.semicolon.boundedContext.order.app.usecase.FindAdminOrderListUseCase;
import dukku.semicolon.boundedContext.order.app.usecase.FindConfirmedItemsUseCase;
import dukku.semicolon.boundedContext.order.app.usecase.FindSellerOrderListUseCase;
import dukku.semicolon.shared.order.dto.AdminOrderSearchCondition;
import dukku.semicolon.shared.order.dto.OrderListResponse;
import dukku.semicolon.shared.order.dto.SellerOrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderFacade {
    private final FindAdminOrderListUseCase findAdminOrderListUseCase;
    private final FindSellerOrderListUseCase findSellerOrderListUseCase;
    private final FindConfirmedItemsUseCase findConfirmedItemsUseCase;

    // 1. 전체 주문 목록 조회 (검색 조건 포함)
    public Page<OrderListResponse> findAllOrders(AdminOrderSearchCondition condition, Pageable pageable) {
        return findAdminOrderListUseCase.execute(condition, pageable);
    }

    // 2. 특정 판매자의 판매 내역 조회
    public Page<SellerOrderItemResponse> findSellerSalesHistory(UUID targetSellerUuid, Pageable pageable) {
        return findSellerOrderListUseCase.execute(targetSellerUuid, pageable);
    }

    // 3. 정산용 구매 확정 목록 조회
    public List<ConfirmedOrderItemResponse> findConfirmedItems(LocalDateTime start, LocalDateTime end) {
        return findConfirmedItemsUseCase.execute(start, end);
    }
}
