package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "product_sellers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_sellers_user_uuid", columnNames = {"user_uuid"})
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSeller extends BaseIdAndUUIDAndTime {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid", comment = "유저 UUID")
    private UUID userUuid;

    @Column(columnDefinition = "TEXT", comment = "상점 소개")
    private String intro;

    @Column(nullable = false, comment = "누적 판매 횟수")
    private Integer salesCount;

    @Column(nullable = false, comment = "현재 판매중 상품 수")
    private Integer activeListingCount;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (this.salesCount == null) this.salesCount = 0;
        if (this.activeListingCount == null) this.activeListingCount = 0;
    }
}
