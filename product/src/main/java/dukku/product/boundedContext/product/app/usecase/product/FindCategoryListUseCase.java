package dukku.product.boundedContext.product.app.usecase.product;

import dukku.product.boundedContext.product.out.CategoryRepository;
import dukku.common.shared.product.dto.product.CategoryCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindCategoryListUseCase {
    private final CategoryRepository categoryRepository;

    public List<CategoryCreateResponse> execute() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryCreateResponse.builder()
                        .id(category.getId())
                        .name(category.getCategoryName())
                        .parentId(category.getParent() == null ? null : category.getParent().getId())
                        .depth(category.getDepth())
                        .build())
                .toList();
    }
}
