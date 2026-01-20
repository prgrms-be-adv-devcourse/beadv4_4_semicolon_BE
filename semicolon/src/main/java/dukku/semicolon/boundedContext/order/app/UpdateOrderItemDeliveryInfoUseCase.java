package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.ForbiddenException;
import dukku.common.global.exception.NotFoundException;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import dukku.semicolon.shared.order.dto.DeliveryInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateOrderItemDeliveryInfoUseCase {
    private final OrderItemRepository orderItemRepository;

    public void execute(UUID orderItemUuid, DeliveryInfoRequest request) {
        OrderItem orderItem = orderItemRepository.findByUuid(orderItemUuid)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 주문 상품입니다."));

        if (!UserUtil.isAdmin() && !orderItem.getSellerUuid().equals(UserUtil.getUserId())) {
            throw new ForbiddenException("주문 수정 권한이 없습니다.");
        }

        orderItem.updateDeliveryInfo(request);
    }
}
