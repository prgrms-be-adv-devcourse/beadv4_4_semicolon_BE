package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.shared.product.dto.ShopProductListResponse;
import dukku.semicolon.shared.product.dto.ShopResponse;
import dukku.semicolon.shared.product.dto.UpdateShopRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShopFacade {

    private final FindMyShopUseCase findMyShopUseCase;
    private final UpdateMyShopUseCase updateMyShopUseCase;
    private final FindShopUseCase findShopUseCase;
    private final FindShopProductsUseCase findShopProductsUseCase;

    public ShopResponse findMyShop(UUID userUuid) {
        return findMyShopUseCase.execute(userUuid);
    }

    public ShopResponse updateMyShop(UUID userUuid, UpdateShopRequest request) {
        return updateMyShopUseCase.execute(userUuid, request);
    }

    public ShopResponse findShop(UUID shopUuid) {
        return findShopUseCase.execute(shopUuid);
    }

    public ShopProductListResponse findShopProducts(UUID shopUuid, int page, int size) {
        return findShopProductsUseCase.execute(shopUuid, page, size);
    }
}
