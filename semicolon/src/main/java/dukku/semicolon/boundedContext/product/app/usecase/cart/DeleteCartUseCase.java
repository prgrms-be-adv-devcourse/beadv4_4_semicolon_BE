package dukku.semicolon.boundedContext.product.app.usecase.cart;

import dukku.common.global.exception.NotFoundException;
import dukku.semicolon.boundedContext.product.entity.Cart;
import dukku.semicolon.boundedContext.product.out.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class DeleteCartUseCase {
    private final CartRepository cartRepository;

    public void execute(UUID userUuid, int cartId) {
        Cart cart = cartRepository.findByIdAndUser_UserUuid(cartId, userUuid)
                .orElseThrow(() -> new NotFoundException("해당 장바구니 항목을 찾을 수 없습니다."));

        cartRepository.delete(cart);
    }
}
