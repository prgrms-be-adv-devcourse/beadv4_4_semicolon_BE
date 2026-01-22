package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductCreateRequest;
import dukku.semicolon.shared.product.exception.InvalidProductCreateRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductSupport productSupport;
    private final ProductRepository productRepository;

    public Product execute(UUID sellerUuid, ProductCreateRequest request) {

        Category category = productSupport.findCategory(request.categoryId());

        Product product = Product.create(
                sellerUuid,
                category,
                request.title(),
                request.description(),
                request.price(),
                request.shippingFee(),
                request.conditionStatus()
        );

        var imageUrls = request.imageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            productSupport.validateImageCount(product.getImages().size(), imageUrls.size());
            imageUrls.forEach(product::addImage);
        }

        return productRepository.save(product);
    }
}
