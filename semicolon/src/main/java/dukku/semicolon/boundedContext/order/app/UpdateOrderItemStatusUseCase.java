package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.ForbiddenException;
import dukku.common.global.exception.NotFoundException;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateOrderItemStatusUseCase {
    private final OrderItemRepository orderItemRepository;

    public void execute(UUID orderItemUuid, OrderItemStatus newStatus) {
        if (newStatus == null) {
            return;
        }

        OrderItem orderItem = orderItemRepository.findByUuid(orderItemUuid)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 주문 상품입니다."));

        if (UserUtil.isAdmin()) {
            orderItem.updateOrderStatus(newStatus);

            return;
        }

        validateUserPermission(orderItem, newStatus);
        orderItem.updateOrderStatus(newStatus);
    }

    private void validateUserPermission(OrderItem orderItem, OrderItemStatus newStatus) {
        if (!orderItem.getOrder().getUserUuid().equals(UserUtil.getUserId())) {
            throw new ForbiddenException("주문 수정 권한이 없습니다.");
        }

        if (!OrderItemStatus.isUserActionAllowed(newStatus)) {
            throw new ForbiddenException("허용되지 않은 주문 상태 변경입니다.");
        }
    }
}
