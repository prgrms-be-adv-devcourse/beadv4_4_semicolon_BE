package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
    private int salesCount;

    @Column(nullable = false, comment = "현재 판매중 상품 수")
    private int activeListingCount;

    public static ProductSeller create(UUID userUuid, String intro) {
        return ProductSeller.builder()
                .userUuid(userUuid)
                .intro(intro)
                .salesCount(0)
                .activeListingCount(0)
                .build();
    }

    public void changeIntro(String intro) {
        this.intro = intro;
    }
}
