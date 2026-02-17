package dukku.common.shared.product.dto.cart;

import java.util.List;

public record CartInternalResponse(List<CartItemInternalDto> items) {

    public static CartInternalResponse from(List<CartItemInternalDto> items) {
        return new CartInternalResponse(items);
    }
}
