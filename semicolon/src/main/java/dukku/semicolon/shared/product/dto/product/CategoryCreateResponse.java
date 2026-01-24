package dukku.semicolon.shared.product.dto.product;

import dukku.semicolon.boundedContext.product.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryCreateResponse {

    private Integer id;
    private String name;
    private Integer depth;
    private Integer parentId;

    public static CategoryCreateResponse from(Category category) {
        return CategoryCreateResponse.builder()
                .id(category.getId())
                .name(category.getCategoryName())
                .depth(category.getDepth())
                .parentId(
                        category.getParent() == null
                                ? null
                                : category.getParent().getId()
                )
                .build();
    }
}
