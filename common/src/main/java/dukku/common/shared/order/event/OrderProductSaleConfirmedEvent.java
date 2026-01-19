package dukku.common.shared.order.event;

import java.util.List;
import java.util.UUID;

public record OrderProductSaleConfirmedEvent(UUID productUuid, List<UUID> productUuids) {

}
