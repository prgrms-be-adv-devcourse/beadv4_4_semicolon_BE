package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.facade.SellerProductFacade;
import dukku.semicolon.shared.product.docs.SellerProductApiDocs;
import dukku.semicolon.shared.product.dto.ProductCreateRequest;
import dukku.semicolon.shared.product.dto.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/products")
@SellerProductApiDocs.SellerProductTag
public class SellerProductController {

    private final SellerProductFacade sellerProductFacade;

    @PostMapping
    @SellerProductApiDocs.CreateProduct
    public ProductDetailResponse create(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        return sellerProductFacade.create(userUuid, request);
    }

    @PatchMapping("/{productUuid}")
    @SellerProductApiDocs.UpdateProduct
    public ProductDetailResponse update(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @PathVariable UUID productUuid,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        return sellerProductFacade.update(userUuid, productUuid, request);
    }

    @DeleteMapping("/{productUuid}")
    @SellerProductApiDocs.DeleteProduct
    public ResponseEntity<Void> delete(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @PathVariable UUID productUuid
    ) {
        sellerProductFacade.delete(userUuid, productUuid);
        return ResponseEntity.noContent().build();
    }
}
