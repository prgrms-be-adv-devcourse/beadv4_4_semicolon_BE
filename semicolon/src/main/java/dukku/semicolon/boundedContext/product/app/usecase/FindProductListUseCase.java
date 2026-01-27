package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FindProductListUseCase {

    private final ProductRepository productRepository;

    public ProductListResponse execute(Integer categoryId, String sort, int page, int size) {

        Sort s = switch (sort == null ? "recent" : sort) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "likeCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50), s);

        Page<Product> result = (categoryId == null)
                ? productRepository.findByVisibilityStatusAndDeletedAtIsNull(VisibilityStatus.VISIBLE, pageable)
                : productRepository.findByCategory_IdAndVisibilityStatusAndDeletedAtIsNull(categoryId, VisibilityStatus.VISIBLE, pageable);

        return ProductListResponse.from(result);
    }
}
