package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductCreateRequest;
import dukku.semicolon.shared.product.exception.ProductCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductSupport productSupport;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Product execute(UUID sellerUuid, ProductCreateRequest request) {

        if (!productSupport.existsByCategoryId(request.getCategoryId())) {
            throw new ProductCategoryNotFoundException();
        }

        Category category = categoryRepository.getReferenceById(request.getCategoryId());
        Product product = Product.create(
                sellerUuid,
                category,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getShippingFee(),
                request.getConditionStatus()
        );

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            productSupport.validateImageCount(product.getImages().size(), imageUrls.size());
            imageUrls.forEach(product::addImage);
        }

        return productRepository.save(product);
    }
}
