package dukku.semicolon.shared.product.exception;

import dukku.common.global.exception.NotFoundException;

public class ProductSellerNotFoundException extends NotFoundException {
    public ProductSellerNotFoundException() {
        super("상점을 찾을 수 없습니다.");
    }
}
