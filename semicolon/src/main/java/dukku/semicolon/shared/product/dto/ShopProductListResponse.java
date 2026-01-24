package dukku.semicolon.shared.product.dto;

import dukku.semicolon.shared.product.dto.product.ProductListItemResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ShopProductListResponse {

    private List<ProductListItemResponse> items;
    private int page;
    private int size;
    private long totalCount;
    private boolean hasNext;

    public static ShopProductListResponse from(Page<?> metaPage, List<ProductListItemResponse> items) {
        return ShopProductListResponse.builder()
                .items(items)
                .page(metaPage.getNumber())
                .size(metaPage.getSize())
                .totalCount(metaPage.getTotalElements())
                .hasNext(metaPage.hasNext())
                .build();
    }
}
