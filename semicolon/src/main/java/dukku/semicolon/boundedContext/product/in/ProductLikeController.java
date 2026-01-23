package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.facade.ProductLikeFacade;
import dukku.semicolon.shared.product.dto.product.LikeProductResponse;
import dukku.semicolon.shared.product.dto.product.MyLikedProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductLikeController {

    private final ProductLikeFacade productLikeFacade;

    @PostMapping("/products/{productUuid}/likes")
    public ResponseEntity<Void> like(@PathVariable UUID productUuid) {
        productLikeFacade.like(productUuid);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products/{productUuid}/likes")
    public LikeProductResponse unlike(
            @PathVariable UUID productUuid,
            @RequestHeader("X-USER-UUID") UUID userUuid
    ) {
        return productLikeFacade.unlike(userUuid, productUuid);
    }

    @GetMapping("/me/likes")
    public MyLikedProductListResponse myLikes(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productLikeFacade.myLikes(userUuid, page, size);
    }
}
