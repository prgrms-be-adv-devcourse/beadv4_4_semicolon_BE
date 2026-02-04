package dukku.semicolon.boundedContext.order.app.usecase;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindConfirmedItemsUseCase {
    private final OrderItemRepository orderItemRepository;

    public List<ConfirmedOrderItemResponse> execute(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("startDateTime must be before endDateTime");
        }

        return orderItemRepository
                .findAllByStatusAndConfirmedAtBetween(
                        OrderItemStatus.CONFIRMED,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(OrderItem::toConfirmedOrderItemResponse)
                .toList();
    }
}
