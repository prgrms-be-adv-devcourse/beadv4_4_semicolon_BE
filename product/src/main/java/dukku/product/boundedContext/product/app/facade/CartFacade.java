package dukku.product.boundedContext.product.app.facade;

import dukku.common.global.UserUtil;
import dukku.common.shared.product.dto.cart.CartDto;
import dukku.common.shared.product.dto.cart.CartInternalResponse;
import dukku.common.shared.product.dto.cart.CartItemInternalDto;
import dukku.common.shared.product.dto.cart.CartListResponse;
import dukku.product.boundedContext.product.app.usecase.cart.CreateCartUseCase;
import dukku.product.boundedContext.product.app.usecase.cart.DeleteAllCartItemUseCase;
import dukku.product.boundedContext.product.app.usecase.cart.DeleteCartUseCase;
import dukku.product.boundedContext.product.app.usecase.cart.FindCartListUseCase;
import dukku.product.boundedContext.product.entity.Cart;
import dukku.product.boundedContext.product.entity.Product;
import dukku.product.boundedContext.product.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartFacade {
    private final CreateCartUseCase createCartUseCase;
    private final DeleteCartUseCase deleteCartUseCase;
    private final FindCartListUseCase findCartListUseCase;
    private final DeleteAllCartItemUseCase deleteAllCartItemUseCase;

    // 장바구니 담기
    public void createCart(UUID productUuid) {
        createCartUseCase.execute(UserUtil.getUserId(), productUuid);
    }

    // 장바구니 상품 삭제 (상품 UUID 기준)
    public void deleteCartItem(int cartId) {
        deleteCartUseCase.execute(UserUtil.getUserId(), cartId);
    }

    // 내 장바구니 조회 (페이징 없음)
    @Transactional(readOnly = true)
    public CartListResponse findMyCartList() {
        UUID userUuid = UserUtil.getUserId();

        List<Cart> carts = findCartListUseCase.execute(userUuid);

        List<CartDto> cartDtos = carts.stream()
                .map(Cart::toDto)
                .toList();

        int totalCount = cartDtos.size();

        long expectedTotalPrice = cartDtos.stream()
                .mapToLong(CartDto::price)
                .sum();

        return new CartListResponse(cartDtos, totalCount, expectedTotalPrice);
    }

    // 특정 유저의 장바구니 조회 (페이징 없음)
    @Transactional(readOnly = true)
    public CartInternalResponse findCartListByUserUuid(UUID userUuid) {
        List<Cart> carts = findCartListUseCase.execute(userUuid);

        List<CartItemInternalDto> items = carts.stream()
                .map(cart -> {

                    Product product = cart.getProduct();

                    String thumbnail = product.getImages().stream()
                            .filter(ProductImage::isThumbnail)
                            .findFirst()
                            .orElseGet(() ->
                                    product.getImages().stream()
                                            .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                                            .findFirst()
                                            .orElse(null)
                            )
                            .getImageUrl();

                    return new CartItemInternalDto(
                            (long) cart.getId(),
                            product.getUuid(),
                            product.getTitle(),
                            product.getPrice(),
                            product.getSaleStatus().name(),
                            thumbnail
                    );
                })
                .toList();

        return CartInternalResponse.from(items);
    }

    // 장바구니 비우기
    public void deleteAllCartItem() {
        deleteAllCartItemUseCase.execute(UserUtil.getUserId());
    }
}
