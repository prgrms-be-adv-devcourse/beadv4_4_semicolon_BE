package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.semicolon.boundedContext.product.out.ProductLikeRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.LikeProductResponse;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnlikeProductUseCase {

    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public LikeProductResponse execute(UUID userUuid, UUID productUuid) {

        if (!productRepository.existsByUuidAndDeletedAtIsNull(productUuid)) {
            throw new ProductNotFoundException();
        }

        productLikeRepository.findByUserUuidAndProduct_Uuid(userUuid, productUuid)
                .ifPresent(productLikeRepository::delete);

        long likeCount = productLikeRepository.countByProduct_Uuid(productUuid);

        return LikeProductResponse.builder()
                .productUuid(productUuid)
                .liked(false)
                .likeCount((int) likeCount)
                .build();
    }
}
