package dukku.semicolon.boundedContext.product.app;

import dukku.semicolon.boundedContext.product.out.ProductLikeRepository;
import dukku.semicolon.shared.product.dto.MyLikedProductListResponse;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindMyLikedProductsUseCase {

    private final ProductLikeRepository productLikeRepository;
    private final ProductSupport productSupport;

    @Transactional(readOnly = true)
    public MyLikedProductListResponse execute(UUID userUuid, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ProductListItemResponse> mapped = productLikeRepository.findByUserUuid(userUuid, pageable)
                .map(like -> productSupport.findListItemByProductUuid(like.getProduct().getUuid()));

        return MyLikedProductListResponse.from(mapped);
    }
}
