package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.shared.product.dto.product.CategoryCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindCategoryListUseCase {

    private final CategoryRepository categoryRepository;

    public List<CategoryCreateResponse> execute() {
        return categoryRepository.findAll().stream()
                .map(CategoryCreateResponse::from)
                .toList();
    }
}
