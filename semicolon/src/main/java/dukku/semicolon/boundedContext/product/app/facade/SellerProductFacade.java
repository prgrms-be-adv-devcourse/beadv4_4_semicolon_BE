package dukku.semicolon.boundedContext.product.app.facade;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.product.app.usecase.product.CreateProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.product.DeleteProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.product.UpdateProductUseCase;
import dukku.semicolon.shared.product.dto.product.ProductCreateRequest;
import dukku.semicolon.shared.product.dto.product.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.product.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerProductFacade {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductDetailResponse create(ProductCreateRequest request) {
        return createProductUseCase.execute(UserUtil.getUserId(), request);
    }

    public ProductDetailResponse update(UUID productUuid, ProductUpdateRequest request) {
        return updateProductUseCase.execute(UserUtil.getUserId(), productUuid, request);
    }

    public void delete(UUID productUuid) {
        deleteProductUseCase.execute(UserUtil.getUserId(), productUuid);
    }
}
