package dukku.semicolon.boundedContext.cart.out;

import dukku.semicolon.boundedContext.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    // 이미 담은 상품인지 확인
    boolean existsByUserUuidAndProductUuid(UUID userUuid, UUID productUuid);

    // 내 장바구니 전체 조회
    List<Cart> findAllByUserUuid(UUID userUuid);

    // 상품 삭제 (User + Product 조합)
    void deleteByUserUuidAndProductUuid(UUID userUuid, UUID productUuid);

    // 전체 비우기
    void deleteByUserUuid(UUID userUuid);
}