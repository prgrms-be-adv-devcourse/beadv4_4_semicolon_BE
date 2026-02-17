package dukku.product.boundedContext.product.app.support;

import dukku.common.shared.product.dto.cart.CartItemInternalDto;
import dukku.product.boundedContext.product.entity.Cart;
import dukku.product.boundedContext.product.entity.Product;
import dukku.product.boundedContext.product.entity.ProductImage;

import java.util.Comparator;

// 썸네일 규칙/매핑은 mapper로 분리 (여기가 제일 지저분했잖아)
public final class CartItemInternalDtoMapper {
    private CartItemInternalDtoMapper() {}

    public static CartItemInternalDto from(Cart cart) {
        Product product = cart.getProduct();

        String thumbnailUrl = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> product.getImages().stream()
                        .min(Comparator.comparingInt(ProductImage::getSortOrder)))
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return new CartItemInternalDto(
                (long) cart.getId(),
                product.getUuid(),
                product.getTitle(),
                product.getPrice(),
                product.getSaleStatus().name(),
                thumbnailUrl
        );
    }
}
