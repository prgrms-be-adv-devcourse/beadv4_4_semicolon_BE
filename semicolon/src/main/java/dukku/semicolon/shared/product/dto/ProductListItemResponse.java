package dukku.semicolon.shared.product.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductListItemResponse(
        UUID productUuid,
        String title,
        Long price,
        String thumbnailUrl,
        int likeCount
) {}
