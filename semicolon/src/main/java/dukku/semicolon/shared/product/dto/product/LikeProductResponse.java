package dukku.semicolon.shared.product.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LikeProductResponse {
    private UUID productUuid;
    private boolean liked;
    private int likeCount;
}
