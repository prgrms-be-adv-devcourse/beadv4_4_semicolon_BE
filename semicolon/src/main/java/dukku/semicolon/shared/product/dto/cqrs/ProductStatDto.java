package dukku.semicolon.shared.product.dto.cqrs;

public record ProductStatDto(
        int productId,
        long viewCount,
        long likeCount,
        long commentCount
) {}