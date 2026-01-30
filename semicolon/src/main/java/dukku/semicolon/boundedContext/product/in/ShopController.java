package dukku.semicolon.boundedContext.product.in;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.app.facade.ShopFacade;
import dukku.semicolon.shared.product.docs.ShopApiDocs;
import dukku.semicolon.shared.product.dto.ShopProductListResponse;
import dukku.semicolon.shared.product.dto.ShopResponse;
import dukku.semicolon.shared.product.dto.UpdateShopRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops")
@ShopApiDocs.ShopTag
public class ShopController {
    private final ShopFacade shopFacade;

    @GetMapping("/me")
    @ShopApiDocs.FindMyShop
    public ShopResponse findMyShop(
    ) {
        return shopFacade.findMyShop();
    }

    @PatchMapping("/me")
    @ShopApiDocs.UpdateMyShop
    public ShopResponse updateMyShop(
            @RequestBody @Valid UpdateShopRequest request
    ) {
        return shopFacade.updateMyShop(request);
    }

    @GetMapping("/me/products")
    @ShopApiDocs.FindMyShopProducts
    public ShopProductListResponse findMyShopProducts(
            @RequestParam(required = false) SaleStatus saleStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return shopFacade.findMyShopProducts(saleStatus, page, size);
    }

    @GetMapping("/{shopUuid}")
    @ShopApiDocs.FindShop
    public ShopResponse findShop(
            @PathVariable UUID shopUuid
    ) {
        return shopFacade.findShop(shopUuid);
    }

    @GetMapping("/{shopUuid}/products")
    @ShopApiDocs.FindShopProducts
    public ShopProductListResponse findShopProducts(
            @PathVariable UUID shopUuid,
            @RequestParam(required = false) SaleStatus saleStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return shopFacade.findShopProducts(shopUuid, saleStatus, page, size);
    }
}
