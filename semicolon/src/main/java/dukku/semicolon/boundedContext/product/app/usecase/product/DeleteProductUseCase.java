package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.semicolon.boundedContext.product.app.support.ProductSupport;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.shared.product.event.ProductDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteProductUseCase {
    private final ProductSupport productSupport;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID productUuid, UUID sellerUuid) {
        Product product = productSupport.getProduct(productUuid, sellerUuid);
        product.delete();

        eventPublisher.publishEvent(new ProductDeletedEvent(product));
    }
}