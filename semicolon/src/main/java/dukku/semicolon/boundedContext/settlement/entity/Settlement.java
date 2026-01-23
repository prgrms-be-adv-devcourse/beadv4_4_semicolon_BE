package dukku.semicolon.boundedContext.settlement.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Settlement extends BaseIdAndUUIDAndTime {

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "seller_uuid", columnDefinition = "uuid", nullable = false, comment = "판매자 UUID")
    private UUID sellerUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "buyer_uuid", columnDefinition = "uuid", nullable = false, comment = "구매자 UUID")
    private UUID buyerUuid;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "payment_id", columnDefinition = "uuid", nullable = false, comment = "결제 UUID")
    private UUID paymentId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "order_id", columnDefinition = "uuid", nullable = false, comment = "주문 UUID")
    private UUID orderId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "order_item_id", columnDefinition = "uuid", comment = "주문 상품 UUID")
    private UUID orderItemId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "deposit_id", columnDefinition = "uuid", nullable = false, comment = "예치금 UUID")
    private UUID depositId;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, comment = "정산 상태")
    private SettlementStatus settlementStatus;

    @Column(name = "total_amount", nullable = false, columnDefinition = "bigint default 0", comment = "총액")
    private Long totalAmount;

    @Column(name = "settlement_reservation_date", nullable = false, comment = "구매 확정 후 당일 자정")
    private LocalDateTime settlementReservationDate;

    @Column(name = "settlement_amount", nullable = false, columnDefinition = "bigint default 0", comment = "정산 금액")
    private Long settlementAmount;

    @Column(nullable = false, precision = 3, scale = 2, columnDefinition = "decimal(3,2) default 0.05", comment = "수수료율")
    private BigDecimal fee;

    @Column(name = "fee_amount", nullable = false, columnDefinition = "bigint default 0", comment = "수수료 금액")
    private Long feeAmount;

    @Column(name = "completed_at", comment = "정산완료일")
    private LocalDateTime completedAt;


    /* ========= 생성 ========= */

    /**
     * 정산 생성 정적 팩토리 메서드
     * - 수수료/정산금액 계산은 Policy에 위임
     * - 스케줄 계산은 Policy에 위임
     * 
     * @param feeRate 수수료율 (필수, application.yml에서 주입)
     */
    public static Settlement create(
            UUID sellerUuid,
            UUID buyerUuid,
            UUID paymentId,
            UUID orderId,
            UUID orderItemId,
            UUID depositId,
            Long totalAmount,
            BigDecimal feeRate,
            LocalDateTime reservationDate
    ) {
        long feeAmount = SettlementFeePolicy.calculateFeeAmount(totalAmount, feeRate);
        long settlementAmount = totalAmount - feeAmount;

        return Settlement.builder()
                .sellerUuid(sellerUuid)
                .buyerUuid(buyerUuid)
                .paymentId(paymentId)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .depositId(depositId)
                .totalAmount(totalAmount)
                .fee(feeRate)
                .feeAmount(feeAmount)
                .settlementAmount(settlementAmount)
                .settlementStatus(SettlementStatus.PENDING)
                .settlementReservationDate(reservationDate)
                .build();
    }

    /**
     * 정산 생성 (기본 스케줄 사용)
     * 오버로드
     */
    public static Settlement create(
            UUID sellerUuid,
            UUID buyerUuid,
            UUID paymentId,
            UUID orderId,
            UUID orderItemId,
            UUID depositId,
            Long totalAmount,
            BigDecimal requestedFeeRate
    ) {
        return create(
                sellerUuid,
                buyerUuid,
                paymentId,
                orderId,
                orderItemId,
                depositId,
                totalAmount,
                requestedFeeRate,
                SettlementSchedulePolicy.nextReservationDate()
        );
    }

    /* ========= 상태 변경 ========= */

    public void startProcessing() {
        validateForProcessing();
        changeStatus(SettlementStatus.PROCESSING);
    }

    public void complete() {
        changeStatus(SettlementStatus.SUCCESS);
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        changeStatus(SettlementStatus.FAILED);
    }

    public void retry() {
        changeStatus(SettlementStatus.PENDING);
    }

    private void changeStatus(SettlementStatus newStatus) {
        if (!this.settlementStatus.canTransitTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("정산 상태를 %s에서 %s로 변경할 수 없습니다.",
                            this.settlementStatus.getStatus(), newStatus.getStatus())
            );
        }
        this.settlementStatus = newStatus;
    }

    /* ========= 검증 로직 ========= */

    /**
     * 예치금 충전 처리 전 검증
     * - PROCESSING 상태로 전이하기 전에 호출됨
     */
    private void validateForProcessing() {
        validateAmount();
        validateReservationDate();
        validateRequiredFields();
    }

    /**
     * 금액 유효성 검증
     */
    private void validateAmount() {
        if (this.totalAmount == null || this.totalAmount <= 0) {
            throw SettlementValidationException.invalidAmount(this.totalAmount);
        }
        if (this.settlementAmount == null || this.settlementAmount <= 0) {
            throw SettlementValidationException.invalidAmount(this.settlementAmount);
        }
        if (this.feeAmount == null || this.feeAmount < 0) {
            throw SettlementValidationException.invalidAmount(this.feeAmount);
        }
        // 총액 = 정산금액 + 수수료
        if (!this.totalAmount.equals(this.settlementAmount + this.feeAmount)) {
            throw new SettlementValidationException(
                    String.format("금액 계산이 올바르지 않습니다. totalAmount=%d, settlementAmount=%d, feeAmount=%d",
                            this.totalAmount, this.settlementAmount, this.feeAmount)
            );
        }
    }

    /**
     * 정산 예약일 검증
     */
    private void validateReservationDate() {
        if (this.settlementReservationDate == null) {
            throw SettlementValidationException.missingRequiredField("settlementReservationDate");
        }
        // 정산 예약일이 과거인지 확인 (현재 시간 기준으로 처리 가능한지)
        if (this.settlementReservationDate.isAfter(LocalDateTime.now())) {
            throw new SettlementValidationException(
                    String.format("정산 예약일이 아직 도래하지 않았습니다. reservationDate=%s, now=%s",
                            this.settlementReservationDate, LocalDateTime.now())
            );
        }
    }

    /**
     * 필수 필드 검증
     */
    private void validateRequiredFields() {
        if (this.sellerUuid == null) {
            throw SettlementValidationException.missingRequiredField("sellerUuid");
        }
        if (this.depositId == null) {
            throw SettlementValidationException.missingRequiredField("depositId");
        }
        if (this.orderId == null) {
            throw SettlementValidationException.missingRequiredField("orderId");
        }
    }

    /* ========= 상태 확인 ========= */

    public boolean isPending() {
        return this.settlementStatus == SettlementStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.settlementStatus == SettlementStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.settlementStatus == SettlementStatus.FAILED;
    }
}
