package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.facade.ProductLikeFacade;
import dukku.semicolon.shared.product.docs.ProductLikeApiDocs;
import dukku.semicolon.shared.product.dto.LikeProductResponse;
import dukku.semicolon.shared.product.dto.MyLikedProductListResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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
    public LikeProductResponse like(
            @PathVariable UUID productUuid,
            @RequestHeader("X-USER-UUID") UUID userUuid // TODO : 임시 사용자 UUID 헤더
    ) {
        return productLikeFacade.like(userUuid, productUuid);
    }

    @DeleteMapping("/products/{productUuid}/likes")
    @ProductLikeApiDocs.UnlikeProduct
    public LikeProductResponse unlike(
            @PathVariable UUID productUuid,
            @RequestHeader("X-USER-UUID") UUID userUuid
    ) {
        return productLikeFacade.unlike(userUuid, productUuid);
    }

    @GetMapping("/me/likes")
    @ProductLikeApiDocs.FindMyLikes
    public MyLikedProductListResponse findMyLikes(
            @RequestHeader("X-USER-UUID") UUID userUuid,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return productLikeFacade.findMyLikes(userUuid, page, size);
    }
}
