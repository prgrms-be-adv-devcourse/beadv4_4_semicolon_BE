package dukku.semicolon.boundedContext.product.app;

import dukku.common.shared.product.type.SaleStatus;
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
    private final FindMyShopProductsUseCase findMyShopProductsUseCase;

    public ShopResponse findMyShop(UUID userUuid) {
        return findMyShopUseCase.execute(userUuid);
    }

    public ShopProductListResponse findMyShopProducts(UUID userUuid, SaleStatus saleStatus, int page, int size) {
        return findMyShopProductsUseCase.execute(userUuid, saleStatus, page, size);
    }

    public ShopResponse updateMyShop(UUID userUuid, UpdateShopRequest request) {
        return updateMyShopUseCase.execute(userUuid, request);
    }

    public ShopResponse findShop(UUID shopUuid) {
        return findShopUseCase.execute(shopUuid);
    }

    public ShopProductListResponse findShopProducts(UUID shopUuid, SaleStatus saleStatus, int page, int size) {
        return findShopProductsUseCase.execute(shopUuid, saleStatus, page, size);
    }
}
