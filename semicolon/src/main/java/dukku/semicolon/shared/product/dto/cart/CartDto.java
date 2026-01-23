package dukku.semicolon.shared.product.dto.cart;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.entity.Cart;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

public record CartDto(
        int cartId,            // 장바구니 PK (삭제 시 사용)
        UUID productUuid,       // 상품 UUID (상세 페이지 이동용)
        String title,           // 상품 제목
        long price,             // 현재 상품 가격 (실시간 반영)
        SaleStatus saleStatus,  // 판매 상태 (ON_SALE, SOLD_OUT 등)
        String thumbnailUrl,    // 썸네일 이미지 URL
        LocalDateTime createdAt // 장바구니에 담은 날짜
) {
    public static CartDto toDto(Cart cart) {
        Product product = cart.getProduct(); // 지연 로딩 없이 바로 접근 가능

        /*
        썸네일 이미지 결정 로직
        1순위: isThumbnail = true인 이미지
        2순위: sortOrder가 가장 낮은(1번) 이미지
        3순위: null (이미지가 아예 없을 때)
        */
        String thumbnailUrl = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElseGet(() ->
                                product.getImages().stream()
                                        .min(Comparator.comparingInt(ProductImage::getSortOrder))
                                        .map(ProductImage::getImageUrl)
                                        .orElse(null)
                );

        return new CartDto(
                cart.getId(),
                product.getUuid(),
                product.getTitle(),
                product.getPrice(),
                product.getSaleStatus(),
                thumbnailUrl,
                cart.getCreatedAt()
        );
    }
}