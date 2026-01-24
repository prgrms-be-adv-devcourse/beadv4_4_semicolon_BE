package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.facade.ProductLikeFacade;
import dukku.semicolon.shared.product.docs.ProductLikeApiDocs;
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
@ProductLikeApiDocs.ProductLikeTag
public class ProductLikeController {

    private final ProductLikeFacade productLikeFacade;

    @PostMapping("/products/{productUuid}/likes")
    @ProductLikeApiDocs.LikeProduct
    public ResponseEntity<Void> like(@PathVariable UUID productUuid) {
        productLikeFacade.like(productUuid);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products/{productUuid}/likes")
    @ProductLikeApiDocs.UnlikeProduct
    public ResponseEntity<Void> unlike(
            @PathVariable UUID productUuid
    ) {
        productLikeFacade.unlike(productUuid);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/likes")
    @ProductLikeApiDocs.MyLikes
    public MyLikedProductListResponse myLikes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productLikeFacade.myLikes(page, size);
    }
}
