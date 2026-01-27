package dukku.semicolon.boundedContext.product.app.usecase;

import dukku.semicolon.boundedContext.product.entity.ProductSeller;
import dukku.semicolon.boundedContext.product.out.ProductSellerRepository;
import dukku.semicolon.shared.product.dto.ShopResponse;
import dukku.semicolon.shared.product.dto.UpdateShopRequest;
import dukku.semicolon.shared.product.exception.ProductSellerNotFoundException;
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

        return ShopResponse.from(seller);
    }
}
