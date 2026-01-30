package dukku.semicolon.boundedContext.product.app.usecase.cart;

import dukku.semicolon.boundedContext.product.out.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteAllCartItemUseCase {
    private final CartRepository cartRepository;

    public void execute(UUID userUuid){
        cartRepository.deleteByUser_UserUuid(userUuid);
    }
}
