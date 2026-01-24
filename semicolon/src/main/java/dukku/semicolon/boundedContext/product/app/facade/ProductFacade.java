package dukku.semicolon.boundedContext.product.app.facade;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import dukku.semicolon.boundedContext.product.app.cqrs.SearchProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.product.*;
import dukku.semicolon.shared.product.dto.cqrs.ProductSearchRequest;
import dukku.semicolon.shared.product.dto.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFacade {
    private final FindCategoryListUseCase findCategoryListUseCase;
    private final FindFeaturedProductsUseCase findFeaturedProductsUseCase;
    private final FindProductListUseCase findProductListUseCase;
    private final FindProductDetailUseCase findProductDetailUseCase;
    private final ReserveProductUseCase reserveProductUseCase;
    private final SearchProductUseCase searchProductUseCase;

    public List<CategoryCreateResponse> findCategories() {
        return findCategoryListUseCase.execute();
    }

    public List<ProductListItemResponse> findFeatured(int size) {
        return findFeaturedProductsUseCase.execute(size);
    }

    public ProductListResponse findProducts(ProductSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));

        try{
            return searchProductUseCase.searchProducts(request, pageable);
        } catch(ElasticsearchException e){
            log.error("ES search failed. request={}", request, e);

            // ES조회 실패 시 DB에서 단순 조회
            return findProductListUseCase.execute(request.getCategoryId(), pageable);
        }
    }

    public ProductDetailResponse findProductDetail(UUID productUuid) {
        return findProductDetailUseCase.execute(productUuid);
    }

    public void reserveProducts(ProductReserveRequest request) {
        reserveProductUseCase.execute(request);
    }
}
