package dukku.product.boundedContext.product.app.usecase.cart;

import dukku.common.shared.product.dto.cart.CartDto;
import dukku.common.shared.product.dto.cart.CartListResponse;
import dukku.product.boundedContext.product.entity.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// /me 응답 조립 전용
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMyCartListUseCase {
    private final FindCartListUseCase findCartListUseCase;

    public CartListResponse execute(UUID userUuid) {
        List<CartDto> items = findCartListUseCase.execute(userUuid).stream()
                .map(Cart::toDto)
                .toList();

        int totalCount = items.size();
        long expectedTotalPrice = items.stream().mapToLong(CartDto::price).sum();

        return new CartListResponse(items, totalCount, expectedTotalPrice);
    }
}
