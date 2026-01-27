package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.semicolon.boundedContext.product.entity.Cart;
import dukku.semicolon.boundedContext.product.out.CartRepository;
import dukku.semicolon.shared.product.dto.CartDto;
import dukku.semicolon.shared.product.dto.CartListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMyCartListUseCase {
    private final CartRepository cartRepository;

    public CartListResponse execute(UUID userUuid) {
        // Fetch Join 쿼리로 조회 (Cart + Product + Images)
        List<Cart> carts = cartRepository.findAllWithProductByUserUuid(userUuid);

        List<CartDto> cartDtos = carts.stream()
                .map(CartDto::toDto)
                .toList();

        int totalCount = cartDtos.size();

        long expectedTotalPrice = cartDtos.stream()
                .mapToLong(CartDto::price)
                .sum();

        return new CartListResponse(cartDtos, totalCount, expectedTotalPrice);
    }
}
