package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductCreateRequest;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.exception.ProductCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSupport productSupport;

    @Transactional
    public ProductDetailResponse execute(UUID userUuid, ProductCreateRequest request) {

        Integer categoryId = request.getCategoryId();

        if (!categoryRepository.existsById(categoryId)) {
            throw new ProductCategoryNotFoundException();
        }

        Category category = categoryRepository.getReferenceById(categoryId);

        Product product = Product.create(
                userUuid,
                category,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getShippingFee(),
                request.getConditionStatus()
        );

        // TODO : 이미지 URL 저장 (S3 등 외부 스토리지 연동 시 수정 필요)
        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            productSupport.validateMaxImageCount(imageUrls.size()); // 신규니까 기존 0
            imageUrls.forEach(product::addImage);
        }

        Product saved = productRepository.save(product);
        return ProductMapper.toDetail(saved);
    }
}
