package dukku.semicolon.shared.product.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class MyLikedProductListResponse {

    private List<ProductListItemResponse> items;
    private int page;
    private int size;
    private long totalCount;
    private boolean hasNext;

    public static MyLikedProductListResponse from(org.springframework.data.domain.Page<ProductListItemResponse> pageResult) {
        return MyLikedProductListResponse.builder()
                .items(pageResult.getContent())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalCount(pageResult.getTotalElements())
                .hasNext(pageResult.hasNext())
                .build();
    }

    public static MyLikedProductListResponse from(Page<?> metaPage, List<ProductListItemResponse> items) {
        return MyLikedProductListResponse.builder()
                .items(items)
                .page(metaPage.getNumber())
                .size(metaPage.getSize())
                .totalCount(metaPage.getTotalElements())
                .hasNext(metaPage.hasNext())
                .build();
    }
}
