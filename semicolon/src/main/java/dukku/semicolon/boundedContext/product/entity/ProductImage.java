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
    private Integer sortOrder;

    @Column(nullable = false, comment = "썸네일 여부")
    private Boolean isThumbnail;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (this.sortOrder == null) this.sortOrder = 1;
        if (this.isThumbnail == null) this.isThumbnail = false;
    }
}
