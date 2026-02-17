package dukku.common.shared.product.dto.cart;

import java.util.UUID;

public record CartItemInternalDto(
        Long cartId,
        UUID productUuid,
        String productTitle,
        long productPrice,
        String saleStatus,
        String thumbnailUrl
) {}
