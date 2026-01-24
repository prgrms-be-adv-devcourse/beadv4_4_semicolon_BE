package dukku.semicolon.shared.product.dto.cqrs;

import dukku.common.shared.product.type.ConditionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ProductSearchRequest {
    private final String keyword;
    private final Integer categoryId;
    private final Long minPrice;
    private final Long maxPrice;
    private final ConditionStatus conditionStatus;
    private final ProductSortType sortType;

    public ProductSearchRequest(
            String keyword,
            Integer categoryId,
            Long minPrice,
            Long maxPrice,
            ConditionStatus conditionStatus,
            ProductSortType sortType
    ) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.conditionStatus = conditionStatus;
        this.sortType = (sortType != null) ? sortType : ProductSortType.LATEST;
    }
}
