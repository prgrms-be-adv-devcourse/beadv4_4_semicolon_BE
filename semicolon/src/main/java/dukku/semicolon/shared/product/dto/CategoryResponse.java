package dukku.semicolon.shared.product.dto;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Integer id,
        String name,
        int depth,
        Integer parentId
) {}
