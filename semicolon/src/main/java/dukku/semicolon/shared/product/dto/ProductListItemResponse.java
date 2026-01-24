package dukku.semicolon.shared.product.dto;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.UUID;

@Getter
@Builder
public class ProductListItemResponse {

    private UUID productUuid;
    private String title;
    private Long price;
    private String thumbnailUrl;
    private int likeCount;

    public static ProductListItemResponse from(Product product) {
        String thumbnail = product.getImages() == null
                ? null
                : product.getImages().stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return ProductListItemResponse.builder()
                .productUuid(product.getUuid())
                .title(product.getTitle())
                .price(product.getPrice())
                .thumbnailUrl(thumbnail)
                .likeCount(product.getLikeCount())
                .build();
    }
}
