package dukku.common.shared.order.event;

import java.util.UUID;

public record OrderItemCanceledEvent(UUID orderItemUuid) {
}
