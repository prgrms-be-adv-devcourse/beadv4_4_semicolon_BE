package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindProductDetailUseCase {

    private final ProductRepository productRepository;

    public ProductDetailResponse execute(UUID productUuid) {
        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        return ProductMapper.toDetail(product);
    }
}
