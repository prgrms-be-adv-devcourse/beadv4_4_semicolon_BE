package dukku.product.boundedContext.product.app.usecase.cart;

import dukku.product.boundedContext.product.entity.Cart;
import dukku.product.boundedContext.product.out.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindCartListUseCase {
    private final CartRepository cartRepository;

    public List<Cart> execute(UUID userUuid) {
        // Fetch Join 쿼리로 조회 (Cart + Product + Images)
        return cartRepository.findAllWithProductByUserUuid(userUuid);
    }
}
