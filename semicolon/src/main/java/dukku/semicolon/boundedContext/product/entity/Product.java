package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.shared.product.dto.ProductUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(
                        name = "idx_products_cat_vis_sale_created",
                        columnList = "category_id, visibility_status, sale_status, created_at"
                ),
                @Index(
                        name = "idx_products_cat_vis_sale_like",
                        columnList = "category_id, visibility_status, sale_status, like_count"
                )
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseIdAndUUIDAndTime {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column( nullable = false, columnDefinition = "uuid", comment = "판매자 UUID")
    private UUID sellerUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, comment = "카테고리")
    private Category category;

    @Column(nullable = false, length = 200, comment = "상품 제목")
    private String title;

    @Column(columnDefinition = "TEXT", comment = "상품 설명")
    private String description;

    @Column(nullable = false, comment = "상품 가격")
    private Long price;

    @Column(nullable = false, comment = "배송비")
    private Long shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            comment = "상품 컨디션"
    )
    private ConditionStatus conditionStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            comment = "상품 판매 상태"
    )
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            comment = "상품 노출 상태"
    )
    private VisibilityStatus visibilityStatus;

    @Column(nullable = false, comment = "조회수")
    private int viewCount;

    @Column(nullable = false, comment = "좋아요 수")
    private int likeCount;

    @Column(nullable = false, comment = "댓글 수")
    private int commentCount;

    @Column(comment = "삭제일(소프트 삭제)")
    private LocalDateTime deletedAt;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", comment = "현재 예약 중인 주문 UUID")
    private UUID reservedOrderUuid;

    public static Product create(
            UUID sellerUuid,
            Category category,
            String title,
            String description,
            Long price,
            Long shippingFee,
            ConditionStatus conditionStatus
    ) {
        return Product.builder()
                .sellerUuid(sellerUuid)
                .category(category)
                .title(title)
                .description(description)
                .price(price)
                .shippingFee(shippingFee == null ? 0L : shippingFee)

                .conditionStatus(conditionStatus == null ? ConditionStatus.SEALED : conditionStatus)
                .saleStatus(SaleStatus.ON_SALE)
                .visibilityStatus(VisibilityStatus.VISIBLE)

                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();
    }

    public void reserve(UUID orderUuid) {
        this.saleStatus = SaleStatus.RESERVED;
        this.reservedOrderUuid = orderUuid;
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    public void addImage(String imageUrl) {
        int nextSortOrder = images.stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(0) + 1;

        images.add(ProductImage.create(this, imageUrl, nextSortOrder));
    }

    public void softDelete() {
        this.deletedAt = java.time.LocalDateTime.now();
    }

    // 수정용 change 메서드
    public void changeCategory(Category category) { this.category = category; }
    public void changeTitle(String title) { this.title = title; }
    public void changeDescription(String description) { this.description = description; }
    public void changePrice(Long price) { this.price = price; }
    public void changeShippingFee(Long shippingFee) { this.shippingFee = (shippingFee == null ? 0L : shippingFee); }
    public void changeConditionStatus(ConditionStatus status) { this.conditionStatus = status; }

    public void replaceImages(List<String> imageUrls) {
        this.images.clear();
        int sort = 1;
        for (String url : imageUrls) {
            this.images.add(ProductImage.create(this, url, sort));
            sort++;
        }
    }

    public void applyUpdate(Category category, ProductUpdateRequest req) {
        if (category != null) changeCategory(category);
        if (req.getTitle() != null) changeTitle(req.getTitle());
        if (req.getDescription() != null) changeDescription(req.getDescription());
        if (req.getPrice() != null) changePrice(req.getPrice());
        if (req.getShippingFee() != null) changeShippingFee(req.getShippingFee());
        if (req.getConditionStatus() != null) changeConditionStatus(req.getConditionStatus());

        if (req.getImageUrls() != null) {
            replaceImages(req.getImageUrls()); // null 아님 => 전체교체
        }
    }
}
