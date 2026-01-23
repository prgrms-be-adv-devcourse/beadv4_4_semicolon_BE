package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.ShopFacade;
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
@RequestMapping("/api/v1")
public class ShopController {

    private final ShopFacade shopFacade;

    // 내 상점 조회
    @GetMapping("/me/shop")
    public ShopResponse findMyShop(
            @RequestHeader("X-USER-UUID") UUID userUuid
    ) {
        return shopFacade.findMyShop(userUuid);
    }

    // 내 상점 소개 수정
    @PatchMapping("/me/shop")
    public ShopResponse updateMyShop(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @RequestBody @Valid UpdateShopRequest request
    ) {
        return shopFacade.updateMyShop(userUuid, request);
    }

    // 판매자 상점 조회(공개)
    @GetMapping("/shops/{shopUuid}")
    public ShopResponse findShop(
            @PathVariable UUID shopUuid
    ) {
        return shopFacade.findShop(shopUuid);
    }

    // 판매자 상점 상품 목록(공개)
    @GetMapping("/shops/{shopUuid}/products")
    public ShopProductListResponse findShopProducts(
            @PathVariable UUID shopUuid,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return shopFacade.findShopProducts(shopUuid, page, size);
    }
}
