package dukku.semicolon.boundedContext.product.app;

import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindFeaturedProductsUseCase {

    private final ProductRepository productRepository;

    public List<ProductListItemResponse> execute(int size) {
        var pageable = PageRequest.of(
                0,
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "likeCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return productRepository.findByVisibilityStatusAndDeletedAtIsNull(VisibilityStatus.VISIBLE, pageable)
                .getContent().stream()
                .map(ProductMapper::toListItem)
                .toList();
    }
}
