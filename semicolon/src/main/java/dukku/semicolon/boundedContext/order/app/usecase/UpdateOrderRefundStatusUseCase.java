package dukku.semicolon.boundedContext.order.app.usecase;

import dukku.common.shared.order.type.OrderStatus;
import dukku.semicolon.boundedContext.order.app.support.OrderSupport;
import dukku.semicolon.boundedContext.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateOrderRefundStatusUseCase {
    private final OrderSupport orderSupport;

    @Transactional
    public void updateRefund(UUID orderUuid, OrderStatus newStatus, int refundAmount) {
        Order order = orderSupport.findOrderByUuid(orderUuid);

        if (Objects.nonNull(newStatus)) {
            order.updateOrderStatus(newStatus);
        }

        if (refundAmount > 0) {
            order.updateRefundedAmount(refundAmount);
        }
    }
}
