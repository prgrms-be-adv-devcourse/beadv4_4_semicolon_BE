package dukku.semicolon.shared.product.exception;

import dukku.common.global.exception.ForbiddenException;

public class ProductUnauthorizedException extends ForbiddenException {
    public ProductUnauthorizedException() {
        super("해당 상품에 대한 권한이 없습니다.");
    }
}
