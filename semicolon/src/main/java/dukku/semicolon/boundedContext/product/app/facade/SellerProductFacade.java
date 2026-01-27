package dukku.semicolon.boundedContext.product.app.facade;

import dukku.semicolon.boundedContext.product.app.usecase.CreateProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.DeleteProductUseCase;
import dukku.semicolon.boundedContext.product.app.usecase.UpdateProductUseCase;
import dukku.semicolon.shared.product.dto.ProductCreateRequest;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerProductFacade {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductDetailResponse create(UUID userUuid, ProductCreateRequest request) {
        return createProductUseCase.execute(userUuid, request);
    }

    public ProductDetailResponse update(UUID userUuid, UUID productUuid, ProductUpdateRequest request) {
        return updateProductUseCase.execute(userUuid, productUuid, request);
    }

    public void delete(UUID userUuid, UUID productUuid) {
        deleteProductUseCase.execute(userUuid, productUuid);
    }
}
