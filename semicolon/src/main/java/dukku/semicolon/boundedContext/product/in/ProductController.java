package dukku.semicolon.boundedContext.product.in;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.app.ProductFacade;
import dukku.semicolon.boundedContext.product.app.SearchProductsUseCase;
import dukku.semicolon.shared.product.docs.ProductApiDocs;
import dukku.semicolon.shared.product.dto.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@ProductApiDocs.ProductTag
public class ProductController {

    private final ProductFacade productFacade;
    private final SearchProductsUseCase searchProductsUseCase;

    @GetMapping("/categories")
    @ProductApiDocs.FindCategories
    public List<CategoryCreateResponse> findCategories() {
        return productFacade.findCategories();
    }

    @GetMapping("/products/featured")
    @ProductApiDocs.FindFeaturedProducts
    public List<ProductListItemResponse> findFeaturedProducts(
            @RequestParam(defaultValue = "20") int size
    ) {
        return productFacade.findFeatured(size);
    }

    @GetMapping("/products")
    @ProductApiDocs.FindProductList
    public ProductListResponse findProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productFacade.findProducts(categoryId, sort, page, size);
    }

    @GetMapping("/products/{productUuid}")
    @ProductApiDocs.FindProductDetail
    public ProductDetailResponse findProductDetail(
            @PathVariable UUID productUuid
    ) {
        return productFacade.findProductDetail(productUuid);
    }

    @GetMapping("/products/search")
    public ProductListResponse searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) SaleStatus saleStatus,
            @RequestParam(required = false, defaultValue = "LATEST") ProductSearchCondition.ProductSort sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        ProductSearchCondition cond = ProductSearchCondition.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .saleStatus(saleStatus)
                .sort(sort)
                .build();

        return searchProductsUseCase.execute(cond, page, size);
    }
}
