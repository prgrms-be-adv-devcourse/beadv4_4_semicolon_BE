package dukku.common.shared.product.dto.cart;

import dukku.common.shared.product.type.SaleStatus;

import java.util.UUID;

public record CartItemInternalDto(
        Long cartId,
        UUID productUuid,
        String productTitle,
        long productPrice,
        SaleStatus saleStatus,
        String thumbnailUrl
) {}
