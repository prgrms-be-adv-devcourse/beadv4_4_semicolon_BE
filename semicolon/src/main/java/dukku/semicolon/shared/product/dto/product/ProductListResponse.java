package dukku.semicolon.shared.product.dto.product;

import dukku.semicolon.boundedContext.product.entity.Product;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ProductListResponse {

    private List<ProductListItemResponse> items;
    private int page;
    private int size;
    private long totalCount;
    private boolean hasNext;

    public static ProductListResponse from(Page<Product> result) {
        return ProductListResponse.builder()
                .items(result.getContent().stream()
                        .map(ProductListItemResponse::from)
                        .toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalCount(result.getTotalElements())
                .hasNext(result.hasNext())
                .build();
    }

    public static ProductListResponse fromByQuery(Page<ProductListItemResponse> pageResult) {
        return ProductListResponse.builder()
                .items(pageResult.getContent())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalCount(pageResult.getTotalElements())
                .hasNext(pageResult.hasNext())
                .build();
    }
}
