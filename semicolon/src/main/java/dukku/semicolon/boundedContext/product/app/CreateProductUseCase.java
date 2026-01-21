package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
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

    public Product execute(
            UUID sellerUuid,
            Integer categoryId,
            String title,
            String description,
            Long price,
            Long shippingFee,
            List<String> imageUrls
    ) {
        if (categoryId == null || categoryId < 1) {
            throw new InvalidProductCreateRequestException();
        }

        if (sellerUuid == null || title == null || title.isBlank() || price == null || price < 0) {
            throw new InvalidProductCreateRequestException();
        }

        Category category = productSupport.findCategory(categoryId);

        Product product = Product.create(
                sellerUuid,
                category,
                title,
                description,
                price,
                shippingFee,
                null
        );

        if (imageUrls != null && !imageUrls.isEmpty()) {
            productSupport.validateImageCount(product.getImages().size(), imageUrls.size());
            imageUrls.forEach(product::addImage);
        }

        return productRepository.save(product);
    }
}
