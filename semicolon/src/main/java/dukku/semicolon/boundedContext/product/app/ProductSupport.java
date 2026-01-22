package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import dukku.semicolon.shared.product.exception.ProductImageLimitExceededException;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductSupport {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public boolean existsByCategoryId(Integer categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    public ProductListItemResponse findListItemByProductUuid(UUID productUuid){
        Product product = productRepository.findByUuidAndDeletedAtIsNull(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        return ProductMapper.toListItem(product);
    }

    public void validateImageCount(int currentCount, int addCount) {
        if (currentCount + addCount > 10) {
            throw new ProductImageLimitExceededException();
        }
    }
}
