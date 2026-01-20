package dukku.semicolon.boundedContext.cart.app;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.ConflictException;
import dukku.semicolon.boundedContext.cart.entity.Cart;
import dukku.semicolon.boundedContext.cart.out.CartRepository;
import dukku.semicolon.shared.cart.dto.CartCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateCartUseCase {
    private final CartSupport cartSupport;
    private final CartRepository cartRepository;

    @Transactional
    public void execute(CartCreateRequest req) {
        UUID userUuid = UserUtil.getUserId();

        // 1. 이미 담긴 상품인지 확인 (수량 필드가 없으므로 중복 저장 방지)
        if (cartSupport.exists(userUuid, req.productUuid())) {
            throw new ConflictException("이미 장바구니에 담긴 상품입니다.");
        }

        // 2. 장바구니 생성
        Cart cart = Cart.createCart(UserUtil.getUserId(), req);

        cartRepository.save(cart);
    }
}
