package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.shared.product.dto.CategoryResponse;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import dukku.semicolon.shared.product.dto.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final FindCategoryListUseCase findCategoryListUseCase;
    private final FindFeaturedProductsUseCase findFeaturedProductsUseCase;
    private final FindProductListUseCase findProductListUseCase;
    private final FindProductDetailUseCase findProductDetailUseCase;

    public List<CategoryResponse> findCategories() {
        return findCategoryListUseCase.execute();
    }

    public List<ProductListItemResponse> findFeatured(int size) {
        return findFeaturedProductsUseCase.execute(size);
    }

    public ProductListResponse findProducts(Integer categoryId, String sort, int page, int size) {
        return findProductListUseCase.execute(categoryId, sort, page, size);
    }

    public ProductDetailResponse findProductDetail(UUID productUuid) {
        return findProductDetailUseCase.execute(productUuid);
    }
}
