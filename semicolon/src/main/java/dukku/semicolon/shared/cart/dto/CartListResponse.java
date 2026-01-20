package dukku.semicolon.shared.cart.dto;

import java.util.List;

public record CartListResponse(
        List<CartResponse> items,
        long totalAmount
) {}
