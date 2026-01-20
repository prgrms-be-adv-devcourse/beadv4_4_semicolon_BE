package dukku.semicolon.boundedContext.cart.app;

import dukku.common.global.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteCartUseCase {
    private final CartSupport cartSupport;

    @Transactional
    public void execute(UUID productUuid) {
        // 내 장바구니에서 해당 상품 삭제
        cartSupport.delete(UserUtil.getUserId(), productUuid);
    }
}
