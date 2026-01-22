package dukku.semicolon.shared.product.exception;

import dukku.common.global.exception.NotFoundException;

public class ProductCategoryNotFoundException extends NotFoundException {
    public ProductCategoryNotFoundException() {
        super("존재하지 않는 카테고리입니다.");
    }
}
