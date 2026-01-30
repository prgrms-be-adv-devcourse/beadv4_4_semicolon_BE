package dukku.semicolon.shared.product.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ProductDetailResponse {

    private UUID productUuid;
    private String title;
    private String description;
    private Long price;
    private Long shippingFee;

    private ConditionStatus conditionStatus;
    private SaleStatus saleStatus;
    private VisibilityStatus visibilityStatus;

    private int likeCount;
    private int viewCount;
    private List<String> imageUrls;
    private CategorySummary category;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class CategorySummary {
        private Integer id;
        private String name;
        private int depth;
    }
}
