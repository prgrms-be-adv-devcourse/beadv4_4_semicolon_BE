package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.shared.product.exception.ProductCategoryNotFoundException;
import dukku.semicolon.shared.product.exception.ProductImageLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSupport {

    private final CategoryRepository categoryRepository;

    public Category findCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(ProductCategoryNotFoundException::new);
    }

    public void validateImageCount(int currentCount, int addCount) {
        if (currentCount + addCount > 10) {
            throw new ProductImageLimitExceededException();
        }
    }
}
