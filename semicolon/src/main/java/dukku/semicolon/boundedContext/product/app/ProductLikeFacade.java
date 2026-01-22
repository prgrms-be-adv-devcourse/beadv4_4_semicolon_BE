package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.shared.product.dto.LikeProductResponse;
import dukku.semicolon.shared.product.dto.MyLikedProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductLikeFacade {

    private final LikeProductUseCase likeProductUseCase;
    private final UnlikeProductUseCase unlikeProductUseCase;
    private final FindMyLikedProductsUseCase findMyLikedProductsUseCase;

    public LikeProductResponse like(UUID userUuid, UUID productUuid) {
        return likeProductUseCase.execute(userUuid, productUuid);
    }

    public LikeProductResponse unlike(UUID userUuid, UUID productUuid) {
        return unlikeProductUseCase.execute(userUuid, productUuid);
    }

    public MyLikedProductListResponse myLikes(UUID userUuid, int page, int size) {
        return findMyLikedProductsUseCase.execute(userUuid, page, size);
    }
}
