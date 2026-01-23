package dukku.semicolon.boundedContext.product.app.facade;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.product.app.usecase.product.FindMyLikedProductsUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.product.LikeProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.product.UnlikeProductUseCase;
import dukku.semicolon.shared.product.dto.product.LikeProductResponse;
import dukku.semicolon.shared.product.dto.product.MyLikedProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductLikeFacade {

    private final LikeProductUseCase likeProductUseCase;
    private final UnlikeProductUseCase unlikeProductUseCase;
    private final FindMyLikedProductsUseCase findMyLikedProductsUseCase;

    public void like(UUID productUuid) {
        likeProductUseCase.execute(UserUtil.getUserId(), productUuid);
    }

    public void unlike(UUID productUuid) {
        unlikeProductUseCase.execute(UserUtil.getUserId(), productUuid);
    }

    public MyLikedProductListResponse myLikes(UUID userUuid, int page, int size) {
        return findMyLikedProductsUseCase.execute(userUuid, page, size);
    }
}
