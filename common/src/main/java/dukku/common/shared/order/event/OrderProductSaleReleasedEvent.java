package dukku.common.shared.order.event;

import java.util.List;
import java.util.UUID;

public record OrderProductSaleReleasedEvent(UUID orderUuid, List<UUID> productUuids) {
}
