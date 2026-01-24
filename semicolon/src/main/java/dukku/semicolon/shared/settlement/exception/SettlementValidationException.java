package dukku.semicolon.shared.settlement.exception;

import dukku.common.global.exception.ValidationException;


public class SettlementValidationException extends ValidationException {

    public SettlementValidationException(String message) {
        super(message);
    }

    public static SettlementValidationException invalidAmount(Long amount) {
        return new SettlementValidationException(
                String.format("정산 금액이 유효하지 않습니다: %d", amount));
    }

    public static SettlementValidationException invalidStatusTransition(String from, String to) {
        return new SettlementValidationException(
                String.format("정산 상태 전이가 불가능합니다: %s -> %s", from, to));
    }

    public static SettlementValidationException missingRequiredField(String fieldName) {
        return new SettlementValidationException(
                String.format("필수 값이 누락되었습니다: %s", fieldName));
    }
}
