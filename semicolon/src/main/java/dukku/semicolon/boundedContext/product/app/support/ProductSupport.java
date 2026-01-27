package dukku.semicolon.boundedContext.product.app.support;

import dukku.semicolon.shared.product.exception.ProductImageLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSupport {

    private static final int MAX_IMAGE_COUNT = 10;

    public void validateMaxImageCount(int count) {
        if (count > MAX_IMAGE_COUNT) throw new ProductImageLimitExceededException();
    }
}
