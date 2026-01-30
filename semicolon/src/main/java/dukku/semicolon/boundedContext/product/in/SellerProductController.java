package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.facade.SellerProductFacade;
import dukku.semicolon.shared.product.docs.SellerProductApiDocs;
import dukku.semicolon.shared.product.dto.product.ProductCreateRequest;
import dukku.semicolon.shared.product.dto.product.ProductDetailResponse;
import dukku.semicolon.shared.product.dto.product.ProductUpdateRequest;
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
            @RequestBody @Valid ProductCreateRequest request
    ) {
        return sellerProductFacade.create(request);
    }

    @PatchMapping("/{productUuid}")
    @SellerProductApiDocs.UpdateProduct
    public ProductDetailResponse update(
            @PathVariable UUID productUuid,
            @RequestBody @Valid ProductUpdateRequest request
    ) {
        return sellerProductFacade.update(productUuid, request);
    }

    @DeleteMapping("/{productUuid}")
    @SellerProductApiDocs.DeleteProduct
    public ResponseEntity<Void> delete(
            @PathVariable UUID productUuid
    ) {
        sellerProductFacade.delete(productUuid);

        return ResponseEntity.noContent().build();
    }
}
