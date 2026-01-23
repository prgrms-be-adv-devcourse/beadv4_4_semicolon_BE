package dukku.semicolon.shared.product.dto.product;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;
import dukku.semicolon.boundedContext.product.entity.query.ProductDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Getter
@Builder
public class ProductListItemResponse {
    private UUID productUuid;
    private String title;
    private Long price;
    private String thumbnailUrl;
    private SaleStatus saleStatus;
    private LocalDateTime createdAt;
    private int likeCount;
    private int viewCount;
    private int commentCount;

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
                .saleStatus(product.getSaleStatus())
                .likeCount(product.getLikeCount())
                .commentCount(product.getCommentCount())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public static ProductListItemResponse from(ProductDocument doc) {
        return ProductListItemResponse.builder()
                .productUuid(UUID.fromString(doc.getProductUuid())) // String UUID -> UUID 변환
                .title(doc.getTitle())
                .price(doc.getPrice())
                .thumbnailUrl(doc.getThumbnailImageUrl())
                .saleStatus(doc.getSaleStatus())
                .likeCount(doc.getLikeCount())
                .commentCount(doc.getCommentCount())
                .viewCount(doc.getViewCount())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
