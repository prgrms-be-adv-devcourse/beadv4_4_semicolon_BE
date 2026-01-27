package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.semicolon.boundedContext.product.app.support.ProductMapper;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductLikeRepository;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.MyLikedProductListResponse;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FindMyLikedProductsUseCase {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public MyLikedProductListResponse execute(UUID userUuid, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<UUID> likedUuidsPage =
                productLikeRepository.findProductUuidsByUserUuid(userUuid, pageable);

        List<UUID> likedUuids = likedUuidsPage.getContent();
        if (likedUuids.isEmpty()) {
            return MyLikedProductListResponse.from(likedUuidsPage, List.of());
        }

        List<Product> products = productRepository.findByUuidInAndDeletedAtIsNull(likedUuids);

        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getUuid, Function.identity()));

        List<ProductListItemResponse> items = likedUuids.stream()
                .map(productMap::get)
                .filter(Objects::nonNull) // 중간 삭제된 상품은 제외
                .map(ProductMapper::toListItem)
                .toList();

        return MyLikedProductListResponse.from(likedUuidsPage, items);
    }
}
