package dukku.semicolon.boundedContext.cart.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.cart.entity.Cart;
import dukku.semicolon.shared.cart.dto.CartListResponse;
import dukku.semicolon.shared.cart.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindMyCartListUseCase {
    private final CartSupport cartSupport;

    @Transactional(readOnly = true)
    public CartListResponse execute() {
        List<Cart> carts = cartSupport.findAllByUserId(UserUtil.getUserId());

        List<CartResponse> items = carts.stream()
                .map(CartResponse::from)
                .toList();

        // 총 금액 계산
        long totalAmount = items.stream().mapToLong(CartResponse::productPrice).sum();

        return new CartListResponse(items, totalAmount);
    }
}