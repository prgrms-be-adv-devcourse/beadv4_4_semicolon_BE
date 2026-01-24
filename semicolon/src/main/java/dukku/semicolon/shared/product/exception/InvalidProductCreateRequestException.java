package dukku.semicolon.shared.product.exception;

public class InvalidProductCreateRequestException extends ProductBadRequestException {
    public InvalidProductCreateRequestException() {
        super("상품 생성 요청 값이 올바르지 않습니다.");
    }
}
