package dukku.semicolon.boundedContext.product.in;

import dukku.semicolon.boundedContext.product.app.CartFacade;
import dukku.semicolon.shared.product.docs.CartApiDocs;
import dukku.semicolon.shared.product.dto.CartListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@CartApiDocs.CartTag
public class CartController {

    private final CartFacade cartFacade;

    @PostMapping("/{productUuid}")
    @CartApiDocs.CreateCart
    public ResponseEntity<Void> createCart(@PathVariable UUID productUuid) {
        cartFacade.createCart(productUuid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{cartId}")
    @CartApiDocs.DeleteCartItem
    public ResponseEntity<Void> deleteCartItem(@PathVariable int cartId) {
        cartFacade.deleteCartItem(cartId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @CartApiDocs.FindMyCartList
    public ResponseEntity<CartListResponse> findMyCartList() {
        CartListResponse response = cartFacade.findMyCartList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @CartApiDocs.DeleteAllCartItem
    public ResponseEntity<Void> deleteAllCartItem() {
        cartFacade.deleteAllCartItem();
        return ResponseEntity.noContent().build();
    }
}
