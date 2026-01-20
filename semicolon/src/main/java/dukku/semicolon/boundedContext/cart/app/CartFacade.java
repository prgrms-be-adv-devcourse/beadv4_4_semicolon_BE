package dukku.semicolon.boundedContext.cart.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.shared.cart.dto.CartCreateRequest;
import dukku.semicolon.shared.cart.dto.CartListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartFacade {
    private final CreateCartUseCase addCartUseCase;
    private final DeleteCartUseCase deleteCartUseCase;
    private final FindMyCartListUseCase findMyCartListUseCase;
    private final CartSupport cartSupport;

    // 장바구니 담기
    public void createCart(CartCreateRequest req) {
        addCartUseCase.execute(req);
    }

    // 장바구니 상품 삭제
    public void deleteCartItem(UUID productUuid) {
        deleteCartUseCase.execute(productUuid);
    }

    // 내 장바구니 조회 (페이징 없음)
    @Transactional(readOnly = true)
    public CartListResponse findMyCartList() {
        return findMyCartListUseCase.execute();
    }

    // 장바구니 비우기
    public void deleteAllCartItem() {
        cartSupport.deleteAllByUserId(UserUtil.getUserId());
    }
}