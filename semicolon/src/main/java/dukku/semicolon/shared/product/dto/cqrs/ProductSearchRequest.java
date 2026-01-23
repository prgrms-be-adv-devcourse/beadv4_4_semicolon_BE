package dukku.semicolon.shared.product.dto.cqrs;

import dukku.common.shared.product.type.ConditionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private Long categoryId;
    private Long minPrice;
    private Long maxPrice;
    private ConditionStatus conditionStatus; // 상품 상태 필터

    // 정렬 기준 추가 (기본값: 최신순)
    private ProductSortType sortType = ProductSortType.LATEST;
}