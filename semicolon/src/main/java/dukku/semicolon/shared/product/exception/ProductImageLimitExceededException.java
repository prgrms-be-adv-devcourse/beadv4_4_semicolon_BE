package dukku.semicolon.shared.product.exception;

import dukku.common.global.exception.BadRequestException;

public class ProductImageLimitExceededException extends BadRequestException {
    public ProductImageLimitExceededException() {
        super("상품 이미지는 최대 10장까지 등록할 수 있습니다.");
    }
}
