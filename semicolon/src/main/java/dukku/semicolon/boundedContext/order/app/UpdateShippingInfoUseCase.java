package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.ForbiddenException;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.shared.order.dto.OrderUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateShippingInfoUseCase {
    private final OrderSupport orderSupport;

    @Transactional
    public void execute(UUID orderUuid, OrderUpdateRequest.ShippingInfo req) {
        Order order = orderSupport.findOrderByUuidWithItems(orderUuid);

        if (!UserUtil.isAdmin() && !order.getUserUuid().equals(UserUtil.getUserId())) {
            throw new ForbiddenException("주문 상품 수정 권한이 없습니다.");
        }

        order.updateOrderForUser(req.getAddress(), req.getRecipient(), req.getContactNumber());
    }
}