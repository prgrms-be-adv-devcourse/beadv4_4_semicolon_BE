package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
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

    // 무작정 바꾸는게 아닌 null이아닌것만 바꾼다.
    public void update(
            Category category,
            String title,
            String description,
            Long price,
            Long shippingFee,
            ConditionStatus conditionStatus,
            VisibilityStatus visibilityStatus
    ) {
        if (category != null) this.category = category;
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (shippingFee != null) this.shippingFee = shippingFee;
        if (conditionStatus != null) this.conditionStatus = conditionStatus;
        if (visibilityStatus != null) this.visibilityStatus = visibilityStatus;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.visibilityStatus = VisibilityStatus.HIDDEN;
    }

    public void replaceImages(List<String> newImageUrls) {
        this.images.clear();

        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            for (String url : newImageUrls) {
                this.addImage(url);
            }
        }
    }

    // 판매 확정 (RESERVED -> SOLD_OUT)
    public void confirmSale(UUID orderUuid) {
        // 내 주문이 맞는지 검증 (다른 사람의 주문으로 예약된 상품을 건드리지 않도록)
        if (this.saleStatus == SaleStatus.RESERVED &&
                this.reservedOrderUuid != null &&
                this.reservedOrderUuid.equals(orderUuid)) {

            this.saleStatus = SaleStatus.SOLD_OUT;
            // 판매 완료되어도 주문 추적을 위해 reservedOrderUuid는 남겨두거나,
            // 별도 soldOrderUuid로 옮기는 정책을 쓸 수 있음. 여기선 유지.
        }
    }

    // 예약 해제 (RESERVED -> ON_SALE)
    public void releaseReservation(UUID orderUuid) {
        // 내 주문이 맞는지 검증
        if (this.saleStatus == SaleStatus.RESERVED &&
                this.reservedOrderUuid != null &&
                this.reservedOrderUuid.equals(orderUuid)) {

            this.saleStatus = SaleStatus.ON_SALE;
            this.reservedOrderUuid = null; // 예약 정보 삭제
        }
    }
}
