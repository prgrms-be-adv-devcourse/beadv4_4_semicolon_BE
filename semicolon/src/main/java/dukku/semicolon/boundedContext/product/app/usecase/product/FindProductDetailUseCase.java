package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.semicolon.boundedContext.product.app.support.ProductMapper;
import dukku.semicolon.boundedContext.product.app.cqrs.ProductStatsRedisSupport;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.product.ProductDetailResponse;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindProductDetailUseCase {
    private final ProductRepository productRepository;
    private final ProductStatsRedisSupport  productStatsRedisSupport;

    public ProductDetailResponse execute(UUID productUuid) {
        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        productStatsRedisSupport.incrementView(product.getId());

        return ProductMapper.toDetail(product);
    }
}
