package dukku.semicolon.shared.product.dto.product;

import java.util.List;
import java.util.UUID;

public record ProductReserveRequest(
        UUID orderUuid,
        List<UUID> productUuids
) {}
