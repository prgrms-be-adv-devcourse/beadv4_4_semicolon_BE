package dukku.common.shared.order.event;

import java.util.UUID;

public record PaymentRollbackRequestEvent(UUID orderUuid, String reason) {
}
