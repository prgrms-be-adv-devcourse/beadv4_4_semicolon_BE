package dukku.semicolon.boundedContext.product.entity;


import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_images_product_sort", columnList = "product_id, sort_order")
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, comment = "상품 ID")
    private Product product;

    @Column(nullable = false, comment = "이미지 URL")
    private String imageUrl;

    @Column(nullable = false, comment = "정렬 순서")
    private int sortOrder;

    @Column(nullable = false, comment = "썸네일 여부")
    private boolean isThumbnail;

    public static ProductImage create(Product product, String imageUrl, int sortOrder) {
        if (product == null) throw new IllegalArgumentException("product는 필수입니다.");
        if (imageUrl == null || imageUrl.isBlank()) throw new IllegalArgumentException("imageUrl은 필수입니다.");
        if (sortOrder < 1) throw new IllegalArgumentException("sortOrder는 1 이상이어야 합니다.");

        return ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .isThumbnail(sortOrder == 1)
                .build();
    }
}
