package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;

import java.util.Comparator;
import java.util.List;

public class ProductMapper {

    public static ProductListItemResponse toListItem(Product p) {
        return ProductListItemResponse.from(p);
    }

    public static ProductDetailResponse toDetail(Product p) {
        List<String> imageUrls = p.getImages().stream()
                .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getImageUrl)
                .toList();

        return ProductDetailResponse.builder()
                .productUuid(p.getUuid())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .shippingFee(p.getShippingFee())
                .likeCount(p.getLikeCount())
                .viewCount(p.getViewCount())
                .imageUrls(imageUrls)
                .conditionStatus(p.getConditionStatus())
                .saleStatus(p.getSaleStatus())
                .visibilityStatus(p.getVisibilityStatus())
                .category(ProductDetailResponse.CategorySummary.builder()
                        .id(p.getCategory().getId())
                        .name(p.getCategory().getCategoryName())
                        .depth(p.getCategory().getDepth())
                        .build())
                .build();
    }
}
