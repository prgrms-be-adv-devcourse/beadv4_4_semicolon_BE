package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductListResponse;
import dukku.semicolon.shared.product.dto.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SearchProductsUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductListResponse execute(ProductSearchCondition cond, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 50)
        );

        Page<Product> result = productRepository.search(cond, pageable);
        return ProductListResponse.from(result);
    }
}
