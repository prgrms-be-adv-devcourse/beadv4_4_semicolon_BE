package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductLike;
import dukku.semicolon.boundedContext.product.out.ProductLikeRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.LikeProductResponse;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LikeProductUseCase {

    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public LikeProductResponse execute(UUID userUuid, UUID productUuid) {

        Product product = productRepository.findByUuidAndDeletedAtIsNull(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        if (!productLikeRepository.existsByUserUuidAndProduct_Uuid(userUuid, productUuid)) {
            try {
                productLikeRepository.save(ProductLike.create(userUuid, product));
            } catch (DataIntegrityViolationException ignored) {
                // 동시성으로 이미 저장된 케이스: 무시하고 계속 진행
            }
        }

        long likeCount = productLikeRepository.countByProduct_Uuid(productUuid);

        return LikeProductResponse.builder()
                .productUuid(productUuid)
                .liked(true)
                .likeCount((int) likeCount)
                .build();
    }
}
