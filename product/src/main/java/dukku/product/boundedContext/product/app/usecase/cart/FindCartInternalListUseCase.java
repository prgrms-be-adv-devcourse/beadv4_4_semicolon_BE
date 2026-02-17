package dukku.product.boundedContext.product.app.usecase.cart;

import dukku.common.shared.product.dto.cart.CartInternalResponse;
import dukku.common.shared.product.dto.cart.CartItemInternalDto;
import dukku.product.boundedContext.product.app.support.CartItemInternalDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// /internal 응답 조립 전용
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindCartInternalListUseCase {
    private final FindCartListUseCase findCartListUseCase;

    public CartInternalResponse execute(UUID userUuid) {
        List<CartItemInternalDto> items = findCartListUseCase.execute(userUuid).stream()
                .map(CartItemInternalDtoMapper::from) // 아래 mapper로 빼면 더 깔끔
                .toList();

        return CartInternalResponse.from(items);
    }
}
