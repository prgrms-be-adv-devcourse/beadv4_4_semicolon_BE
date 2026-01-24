package dukku.semicolon.shared.product.dto.cart;

import java.util.List;

public record CartListResponse(
        List<CartDto> items,
        int totalCount,
        long expectedTotalPrice
) {}