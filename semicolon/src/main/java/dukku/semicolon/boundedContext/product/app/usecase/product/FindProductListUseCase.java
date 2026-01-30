package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.product.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindProductListUseCase {

    private final ProductRepository productRepository;

    public ProductListResponse execute(Integer categoryId, Pageable pageable) {
        Page<Product> result = (categoryId == null)
                ? productRepository.findByVisibilityStatusAndDeletedAtIsNull(VisibilityStatus.VISIBLE, pageable)
                : productRepository.findByCategory_IdAndVisibilityStatusAndDeletedAtIsNull(categoryId, VisibilityStatus.VISIBLE, pageable);

        return ProductListResponse.from(result);
    }
}
