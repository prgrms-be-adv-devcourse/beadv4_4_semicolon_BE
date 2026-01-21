package dukku.semicolon.shared.product.dto;

import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProductDetailResponse(
        UUID productUuid,
        String title,
        String description,
        Long price,
        Long shippingFee,

        ConditionStatus conditionStatus,
        SaleStatus saleStatus,
        VisibilityStatus visibilityStatus,

        int likeCount,
        int viewCount,
        List<String> imageUrls,
        CategorySummary category
) {
    @Builder
    public record CategorySummary(
            Integer id,
            String name,
            int depth
    ) {}
}
