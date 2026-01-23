package dukku.semicolon.shared.product.dto;

import dukku.semicolon.boundedContext.product.entity.ProductSeller;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ShopResponse {
    private UUID shopUuid;              // ProductSeller.uuid
    private String intro;
    private int salesCount;
    private int activeListingCount;

    public static ShopResponse from(ProductSeller seller) {
        return ShopResponse.builder()
                .shopUuid(seller.getUuid())
                .intro(seller.getIntro())
                .salesCount(seller.getSalesCount())
                .activeListingCount(seller.getActiveListingCount())
                .build();
    }
}
