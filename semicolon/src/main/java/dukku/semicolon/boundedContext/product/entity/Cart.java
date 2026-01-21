package dukku.semicolon.boundedContext.product.entity;

import dukku.common.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "carts",
        uniqueConstraints = {
                // 한 유저는 동일한 상품을 중복해서 담을 수 없음
                @UniqueConstraint(
                        name = "uk_carts_user_product",
                        columnNames = {"user_uuid", "product_id"}
                )
        },
        indexes = {
                // 특정 유저의 장바구니 조회 성능 향상
                @Index(name = "idx_carts_user_uuid", columnList = "user_uuid")
        }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseIdAndTime {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false, comment = "구매 희망자 (ProductUser 참조)")
    private ProductUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, comment = "상품 ID")
    private Product product;

    public static Cart createCart(ProductUser user, Product product) {
        return Cart.builder()
                .user(user)
                .product(product)
                .build();
    }
}