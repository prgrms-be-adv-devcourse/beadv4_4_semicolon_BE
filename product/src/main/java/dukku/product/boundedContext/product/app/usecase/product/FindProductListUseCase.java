package dukku.product.boundedContext.product.app.usecase.product;

import dukku.common.shared.product.type.VisibilityStatus;
import dukku.product.boundedContext.product.entity.Product;
import dukku.product.boundedContext.product.out.CategoryRepository;
import dukku.product.boundedContext.product.out.ProductRepository;
import dukku.common.shared.product.dto.product.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindProductListUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductListResponse execute(Integer categoryId, Pageable pageable) {
        Page<Product> result;
        if (categoryId == null) {
            result = productRepository.findByVisibilityStatusAndDeletedAtIsNull(VisibilityStatus.VISIBLE, pageable);
        } else {
            List<Integer> categoryIds = categoryRepository.findCategoryPathIds(categoryId);
            result = productRepository.findByCategory_IdInAndVisibilityStatusAndDeletedAtIsNull(categoryIds, VisibilityStatus.VISIBLE, pageable);
        }

        return ProductListResponse.builder()
                .items(result.getContent().stream()
                        .map(Product::toListItemResponse)
                        .toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalCount(result.getTotalElements())
                .hasNext(result.hasNext())
                .build();
    }
}
