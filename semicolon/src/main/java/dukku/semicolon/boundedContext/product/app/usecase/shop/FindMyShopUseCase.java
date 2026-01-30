package dukku.semicolon.boundedContext.product.app.usecase.shop;

import dukku.semicolon.boundedContext.product.entity.ProductSeller;
import dukku.semicolon.boundedContext.product.out.ProductSellerRepository;
import dukku.semicolon.shared.product.dto.ShopResponse;
import dukku.semicolon.shared.product.exception.ProductSellerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindMyShopUseCase {

    private final ProductSellerRepository productSellerRepository;

    @Transactional(readOnly = true)
    public ShopResponse execute(UUID userUuid) {
        ProductSeller seller = productSellerRepository.findByUserUuid(userUuid)
                .orElseThrow(ProductSellerNotFoundException::new);

        return ShopResponse.from(seller);
    }
}
