package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
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
                ),
                @Index(
                        name = "idx_products_cat_vis_sale_price",
                        columnList = "category_id, visibility_status, sale_status, price"
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
    private Integer price;

    @Column(nullable = false, comment = "배송비")
    private Integer shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "enum('SEALED','NO_WEAR','MINOR_WEAR','VISIBLE_WEAR','DAMAGED')",
            comment = "상품 컨디션"
    )
    private ConditionStatus conditionStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "enum('ON_SALE','RESERVED','SOLD_OUT')",
            comment = "상품 판매 상태"
    )
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "enum('VISIBLE','HIDDEN','BLOCKED')",
            comment = "상품 노출 상태"
    )
    private VisibilityStatus visibilityStatus;

    @Column(nullable = false, comment = "조회수")
    private Integer viewCount;

    @Column(nullable = false, comment = "좋아요 수")
    private Integer likeCount;

    @Column(nullable = false, comment = "댓글 수")
    private Integer commentCount;

    @Column(comment = "삭제일(소프트 삭제)")
    private LocalDateTime deletedAt;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(columnDefinition = "uuid", comment = "현재 예약 중인 주문 UUID")
    private UUID reservedOrderUuid;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (this.shippingFee == null) this.shippingFee = 0;
        if (this.viewCount == null) this.viewCount = 0;
        if (this.likeCount == null) this.likeCount = 0;
        if (this.commentCount == null) this.commentCount = 0;

        if (this.conditionStatus == null) this.conditionStatus = ConditionStatus.SEALED;
        if (this.saleStatus == null) this.saleStatus = SaleStatus.ON_SALE;
        if (this.visibilityStatus == null) this.visibilityStatus = VisibilityStatus.VISIBLE;
    }
}
