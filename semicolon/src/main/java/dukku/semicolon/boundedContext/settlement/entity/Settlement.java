package dukku.semicolon.boundedContext.settlement.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
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
    @Column(name = "payments_id", columnDefinition = "uuid", nullable = false, comment = "결제 UUID")
    private UUID paymentsId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "orders_id", columnDefinition = "uuid", nullable = false, comment = "주문 UUID")
    private UUID ordersId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "deposit_id", columnDefinition = "uuid", nullable = false, comment = "예치금 UUID")
    private UUID depositId;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, columnDefinition = "enum('CREATED','PROCESSING','PENDING','SUCCESS','FAILED')", comment = "정산 상태")
    private SettlementStatus settlementStatus;

    @Column(name = "total_amount", nullable = false, comment = "총액")
    private Integer totalAmount;

    @Column(name = "settlement_reservation_date", nullable = false, comment = "구매 확정 후 당일 자정")
    private LocalDateTime settlementReservationDate;

    @Column(name = "settlement_amount", nullable = false, comment = "정산 금액")
    private Integer settlementAmount;

    @Column(nullable = false, precision = 3, scale = 2, columnDefinition = "decimal(3,2) default 0.90", comment = "수수료율")
    private BigDecimal fee;

    @Column(name = "fee_amount", nullable = false, comment = "수수료 금액")
    private Integer feeAmount;

    @Column(name = "completed_at", comment = "정산완료일")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        super.prePersist(); // 자식이 @PrePersist 사용하면, 부모 클래스도 명시적으로 호출해야함
        if (this.settlementStatus == null) {
            this.settlementStatus = SettlementStatus.PENDING;
        }
        if (this.fee == null) {
            this.fee = new BigDecimal("0.90");
        }
    }
}
