package dukku.semicolon.shared.product.dto;

import dukku.common.shared.product.type.SaleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSearchCondition {
    private String keyword;
    private Integer categoryId;
    private Long minPrice;
    private Long maxPrice;
    private SaleStatus saleStatus;
    private ProductSort sort;

    public enum ProductSort {
        LATEST,
        PRICE_ASC,
        PRICE_DESC,
        LIKE_DESC
    }
}
