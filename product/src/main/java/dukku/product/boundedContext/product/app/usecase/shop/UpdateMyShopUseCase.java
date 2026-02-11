package dukku.product.boundedContext.product.app.usecase.shop;

import dukku.product.boundedContext.product.entity.ProductSeller;
import dukku.product.boundedContext.product.out.ProductSellerRepository;
import dukku.common.shared.product.dto.shop.ShopResponse;
import dukku.common.shared.product.dto.shop.UpdateShopRequest;
import dukku.common.shared.product.exception.ProductSellerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateMyShopUseCase {

    private final ProductSellerRepository productSellerRepository;

    @Transactional
    public ShopResponse execute(UUID userUuid, UpdateShopRequest request) {
        ProductSeller seller = productSellerRepository.findByUserUuid(userUuid)
                .orElseThrow(ProductSellerNotFoundException::new);

        // intro만 수정 (null이면 그대로 유지)
        if (request.getIntro() != null) {
            seller.changeIntro(request.getIntro());
        }

        return ShopResponse.builder()
                .shopUuid(seller.getUuid())
                .intro(seller.getIntro())
                .salesCount(seller.getSalesCount())
                .activeListingCount(seller.getActiveListingCount())
                .build();
    }
}
