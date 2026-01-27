package dukku.semicolon.boundedContext.product.app.facade;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.product.app.usecase.CreateCartUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.DeleteAllCartItemUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.DeleteCartUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.FindMyCartListUseCase;
import dukku.semicolon.shared.product.dto.CartListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartFacade {
    private final CreateCartUseCase createCartUseCase;
    private final DeleteCartUseCase deleteCartUseCase;
    private final FindMyCartListUseCase findMyCartListUseCase;
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
        return findMyCartListUseCase.execute(UserUtil.getUserId());
    }

    // 장바구니 비우기
    public void deleteAllCartItem() {
        deleteAllCartItemUseCase.execute(UserUtil.getUserId());
    }
}
