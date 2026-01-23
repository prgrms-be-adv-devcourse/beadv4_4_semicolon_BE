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
        name = "product_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_likes_user_product", columnNames = {"user_uuid", "product_id"})
        },
        indexes = {
                @Index(name = "idx_product_likes_product", columnList = "product_id"),
                @Index(name = "idx_product_likes_user", columnList = "user_uuid")
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseIdAndUUIDAndTime {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false, columnDefinition = "uuid",  comment = "유저 UUID")
    private UUID userUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "product_id", comment = "상품 ID")
    private Product product;

    public static ProductLike create(UUID userUuid, Product product) {
        return ProductLike.builder()
                .userUuid(userUuid)
                .product(product)
                .build();
    }
}
