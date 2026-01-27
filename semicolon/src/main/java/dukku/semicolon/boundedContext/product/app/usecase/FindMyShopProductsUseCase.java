package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.app.support.ProductMapper;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductSeller;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.boundedContext.product.out.ProductSellerRepository;
import dukku.semicolon.shared.product.dto.ProductListItemResponse;
import dukku.semicolon.shared.product.dto.ShopProductListResponse;
import dukku.semicolon.shared.product.exception.ProductSellerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindMyShopProductsUseCase {

    private final ProductSellerRepository productSellerRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ShopProductListResponse execute(UUID userUuid, SaleStatus saleStatus, int page, int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, 50),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 내 상점: userUuid로 ProductSeller 찾고
        ProductSeller seller = productSellerRepository.findByUserUuid(userUuid)
                .orElseThrow(ProductSellerNotFoundException::new);

        // seller.userUuid == Product.sellerUuid
        UUID sellerUserUuid = seller.getUserUuid();

        Page<Product> result = (saleStatus == null)
                ? productRepository.findBySellerUuidAndDeletedAtIsNull(sellerUserUuid, pageable)
                : productRepository.findBySellerUuidAndSaleStatusAndDeletedAtIsNull(sellerUserUuid, saleStatus, pageable);

        List<ProductListItemResponse> items = result.getContent().stream()
                .map(ProductMapper::toListItem)
                .toList();

        return ShopProductListResponse.from(result, items);
    }
}
