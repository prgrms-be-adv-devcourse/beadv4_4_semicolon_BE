package dukku.semicolon.shared.product.exception;

import dukku.common.global.exception.BadRequestException;

public class ProductBadRequestException extends BadRequestException {
    public ProductBadRequestException(String details) {
        super(details);
    }
}
