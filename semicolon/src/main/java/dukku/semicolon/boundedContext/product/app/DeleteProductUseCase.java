package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.exception.ProductNotFoundException;
import dukku.semicolon.shared.product.exception.ProductUnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(UUID userUuid, UUID productUuid) {

        Product product = productRepository.findByUuidAndDeletedAtIsNull(productUuid)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getSellerUuid().equals(userUuid)) {
            throw new ProductUnauthorizedException();
        }

        product.softDelete();
    }
}
