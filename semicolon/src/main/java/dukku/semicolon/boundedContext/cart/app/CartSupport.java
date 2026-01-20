package dukku.semicolon.boundedContext.cart.app;

import dukku.semicolon.boundedContext.cart.entity.Cart;
import dukku.semicolon.boundedContext.cart.out.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartSupport {
    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public boolean exists(UUID userUuid, UUID productUuid) {
        return cartRepository.existsByUserUuidAndProductUuid(userUuid, productUuid);
    }

    @Transactional(readOnly = true)
    public List<Cart> findAllByUserId(UUID userUuid) {
        return cartRepository.findAllByUserUuid(userUuid);
    }

    public void delete(UUID userUuid, UUID productUuid) {
        cartRepository.deleteByUserUuidAndProductUuid(userUuid, productUuid);
    }

    public void deleteAllByUserId(UUID userUuid) {
        cartRepository.deleteByUserUuid(userUuid);
    }
}