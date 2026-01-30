package dukku.semicolon.boundedContext.product.app.usecase.like;

import dukku.semicolon.boundedContext.product.app.cqrs.ProductStatsRedisSupport;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductLike;
import dukku.semicolon.boundedContext.product.out.ProductLikeRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LikeProductUseCase {
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;
    private final ProductStatsRedisSupport  productStatsRedisSupport;

    @Transactional
    public void execute(UUID userUuid, UUID productUuid) {
        Product product = productRepository.findByUuidAndDeletedAtIsNull(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        if (!productLikeRepository.existsByUserUuidAndProduct_Uuid(userUuid, productUuid)) {
            ProductLike like = productLikeRepository.save(ProductLike.create(userUuid, product));
            productStatsRedisSupport.incrementLike(like.getProduct().getId());
        }
    }
}
