package dukku.semicolon.boundedContext.product.app.facade;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.product.app.usecase.product.*;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.shared.product.dto.product.ProductUpdateRequest;
import dukku.semicolon.shared.product.dto.product.*;
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
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ReserveProductUseCase reserveProductUseCase;

    // TODO: return DTO
    public Product createProduct(ProductCreateRequest request) {
        return createProductUseCase.execute(UserUtil.getUserId(), request);
    }

    public Product updateProduct(UUID productUuid, ProductUpdateRequest request) {
        return updateProductUseCase.execute(productUuid, UserUtil.getUserId(), request);
    }

    public void deleteProduct(UUID productUuid) {
        deleteProductUseCase.execute(productUuid, UserUtil.getUserId());
    }

    public List<CategoryCreateResponse> findCategories() {
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

    public void reserveProducts(ProductReserveRequest request) {
        reserveProductUseCase.execute(request);
    }
}
