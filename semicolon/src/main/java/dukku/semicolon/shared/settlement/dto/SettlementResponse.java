package dukku.semicolon.shared.settlement.dto;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 정산 단건 응답 DTO
 * GET /admin/settlements 목록 조회용
 */
public record SettlementResponse(
        UUID settlementUuid,
        SettlementStatus status,
        UUID sellerUuid,
        String sellerNickname,
        String productName,
        Integer totalAmount,
        BigDecimal fee,
        Integer feeAmount,
        Integer settlementAmount,
        LocalDateTime settlementReservationDate,
        String bankName,
        String accountNumber,
        UUID orderUuid
) {
    /**
     * 엔티티 + 외부 도메인 정보를 조합하여 응답 생성
     * record + of() 조합
     */
    public static SettlementResponse of(
            Settlement settlement,
            String sellerNickname,
            String productName,
            String bankName,
            String accountNumber
    ) {
        return new SettlementResponse(
                settlement.getUuid(),
                settlement.getSettlementStatus(),
                settlement.getSellerUuid(),
                sellerNickname,
                productName,
                settlement.getTotalAmount(),
                settlement.getFee(),
                settlement.getFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getSettlementReservationDate(),
                bankName,
                accountNumber,
                settlement.getOrderId()
        );
    }
}
