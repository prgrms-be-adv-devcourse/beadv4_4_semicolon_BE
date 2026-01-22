package dukku.semicolon.shared.settlement.exception;

import dukku.common.global.exception.BadRequestException;


public class SettlementProcessingException extends BadRequestException {

    public SettlementProcessingException(String message) {
        super(message);
    }

    public static SettlementProcessingException depositAccountNotFound(String sellerUuid) {
        return new SettlementProcessingException(
                String.format("판매자의 예치금 계좌를 찾을 수 없습니다: %s", sellerUuid));
    }

    public static SettlementProcessingException depositChargeFailed(String settlementUuid, String reason) {
        return new SettlementProcessingException(
                String.format("예치금 충전에 실패했습니다. 정산ID: %s, 원인: %s", settlementUuid, reason));
    }

    public static SettlementProcessingException externalServiceFailed(String serviceName, String reason) {
        return new SettlementProcessingException(
                String.format("외부 서비스 호출 실패: %s, 원인: %s", serviceName, reason));
    }
}
