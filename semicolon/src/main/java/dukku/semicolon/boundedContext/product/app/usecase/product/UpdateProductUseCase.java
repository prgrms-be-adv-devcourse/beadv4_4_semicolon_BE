package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.semicolon.boundedContext.product.app.support.ProductSupport;
import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.product.ProductUpdateRequest;
import dukku.semicolon.shared.product.event.ProductUpdatedEvent;
import dukku.semicolon.shared.product.exception.ProductCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 판매자가 바꿀 경우
@Component
@RequiredArgsConstructor
public class UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductSupport productSupport;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Product execute(UUID productUuid, UUID sellerUuid, ProductUpdateRequest request) {
        Product product = productSupport.getProduct(productUuid, sellerUuid);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(ProductCategoryNotFoundException::new);

        product.update(
                category,
                request.title(),
                request.description(),
                request.price(),
                request.shippingFee(),
                request.conditionStatus(),
                request.visibilityStatus()
        );

        if (request.imageUrls() != null) {
            productSupport.validateImageCount(product.getImages().size(), request.imageUrls().size());
            product.replaceImages(request.imageUrls());
        }

        eventPublisher.publishEvent(new ProductUpdatedEvent(product));

        return product;
    }
}