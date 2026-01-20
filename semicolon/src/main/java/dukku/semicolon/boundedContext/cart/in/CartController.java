package dukku.semicolon.boundedContext.cart.in;

import dukku.semicolon.boundedContext.cart.app.CartFacade;
import dukku.semicolon.shared.cart.dto.CartCreateRequest;
import dukku.semicolon.shared.cart.dto.CartListResponse;
import dukku.semicolon.shared.cart.docs.CartApiDocs;
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

    @PostMapping
    @CartApiDocs.CreateCart
    public ResponseEntity<Void> createCart(@RequestBody CartCreateRequest req) {
        cartFacade.createCart(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    @CartApiDocs.FindMyCartList
    public ResponseEntity<CartListResponse> findMyCartList() {
        CartListResponse response = cartFacade.findMyCartList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productUuid}")
    @CartApiDocs.DeleteCartItem
    public ResponseEntity<Void> deleteCartItem(@PathVariable UUID productUuid) {
        cartFacade.deleteCartItem(productUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @CartApiDocs.DeleteAllCartItem
    public ResponseEntity<Void> deleteAllCartItem() {
        cartFacade.deleteAllCartItem();
        return ResponseEntity.noContent().build();
    }
}
