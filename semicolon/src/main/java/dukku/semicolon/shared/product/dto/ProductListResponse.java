package dukku.semicolon.shared.product.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductListResponse(
        List<ProductListItemResponse> items,
        int page,
        int size,
        long totalCount,
        boolean hasNext
) {}
