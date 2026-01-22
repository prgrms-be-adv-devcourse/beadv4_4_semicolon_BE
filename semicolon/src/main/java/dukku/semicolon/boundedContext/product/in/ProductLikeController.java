package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.ProductLikeFacade;
import dukku.semicolon.shared.product.dto.LikeProductResponse;
import dukku.semicolon.shared.product.dto.MyLikedProductListResponse;
import lombok.RequiredArgsConstructor;
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
    public LikeProductResponse like(
            @PathVariable UUID productUuid,
            @RequestHeader("X-USER-UUID") UUID userUuid // 프로젝트 인증 방식에 맞게 바꾸기
    ) {
        return productLikeFacade.like(userUuid, productUuid);
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
