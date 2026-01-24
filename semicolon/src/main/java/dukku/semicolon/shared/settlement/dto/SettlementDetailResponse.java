package dukku.semicolon.shared.settlement.dto;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 정산 상세 응답 DTO (외부 정보 포함)
 * - Settlement 엔티티 정보 + 외부 BC 정보 조합
 * - 관리자 화면 표시용 (판매자 닉네임, 상품명, 계좌 정보 포함)
 */
public record SettlementDetailResponse(
        UUID settlementUuid,
        SettlementStatus status,
        UUID sellerUuid,
        String sellerNickname,      // 외부 User BC에서 조회
        String productName,          // 외부 Product BC에서 조회
        Long totalAmount,
        BigDecimal fee,
        Long feeAmount,
        Long settlementAmount,
        LocalDateTime settlementReservationDate,
        String bankName,             // 외부 Deposit BC에서 조회
        String accountNumber,        // 외부 Deposit BC에서 조회
        UUID orderUuid,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Settlement 엔티티만으로 응답 생성 (외부 정보 null)
     */
    public static SettlementDetailResponse from(Settlement settlement) {
        return new SettlementDetailResponse(
                settlement.getUuid(),
                settlement.getSettlementStatus(),
                settlement.getSellerUuid(),
                null,  // sellerNickname - TODO: UserApiClient 구현 후 사용
                null,  // productName - TODO: ProductApiClient 구현 후 사용
                settlement.getTotalAmount(),
                settlement.getFee(),
                settlement.getFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getSettlementReservationDate(),
                null,  // bankName - TODO: DepositApiClient 구현 후 사용
                null,  // accountNumber - TODO: DepositApiClient 구현 후 사용
                settlement.getOrderId(),
                settlement.getCompletedAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }

    /**
     * Settlement + 외부 BC 정보를 조합하여 응답 생성
     * - API Client 구현 후 사용
     */
    public static SettlementDetailResponse of(
            Settlement settlement,
            String sellerNickname,
            String productName,
            String bankName,
            String accountNumber
    ) {
        return new SettlementDetailResponse(
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
                settlement.getOrderId(),
                settlement.getCompletedAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}
