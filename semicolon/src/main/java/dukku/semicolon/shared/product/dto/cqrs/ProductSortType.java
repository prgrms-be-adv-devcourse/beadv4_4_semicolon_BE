package dukku.semicolon.shared.product.dto.cqrs;

import lombok.Getter;

@Getter
public enum ProductSortType {
    LATEST("createdAt"),       // 최신순
    PRICE_LOW("price"),        // 낮은 가격순
    PRICE_HIGH("price"),       // 높은 가격순
    VIEWS("viewCount"),        // 조회수순
    LIKES("likeCount");        // 좋아요순

    private final String field;

    ProductSortType(String field) {
        this.field = field;
    }

}
