package dukku.semicolon.shared.cart.dto;

import dukku.semicolon.boundedContext.cart.entity.Cart;

import java.util.UUID;

public record CartResponse(
        int cartId,
        UUID productUuid,
        String productName,
        int productPrice,
        String imageUrl
) {
    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getProductUuid(),
                cart.getProductName(),
                cart.getProductPrice(),
                cart.getImageUrl()
        );
    }
}
