package dukku.semicolon.boundedContext.cart.entity;

import dukku.common.global.jpa.entity.BaseIdAndTime;
import dukku.semicolon.shared.cart.dto.CartCreateRequest;
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
        name = "carts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_user_product",
                        columnNames = {"user_uuid", "product_uuid"}
                )
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseIdAndTime {
    @Column(name = "product_name_snapshot", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price_snapshot", nullable = false)
    private int productPrice;

    @Column(name = "image_url_snapshot")
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false)
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(nullable = false)
    private UUID productUuid;

    public static Cart createCart(UUID userUuid, CartCreateRequest request) {
        return Cart.builder()
                .productUuid(request.productUuid())
                .productName(request.productName())
                .productPrice(request.productPrice())
                .imageUrl(request.imageUrl())
                .userUuid(userUuid)
                .build();
    }
}
