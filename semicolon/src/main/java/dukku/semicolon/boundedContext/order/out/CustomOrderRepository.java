package dukku.semicolon.boundedContext.order.out;

import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.shared.order.dto.AdminOrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomOrderRepository {
    // 회원용: 내 주문 + 최신순
    Page<Order> findAllMyOrders(UUID userUuid, Pageable pageable);

    // 관리자용: 전체 검색 + 자유 정렬
    Page<Order> searchForAdmin(AdminOrderSearchCondition condition, Pageable pageable);
}
