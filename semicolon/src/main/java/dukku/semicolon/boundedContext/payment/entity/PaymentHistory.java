package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentHistoryType;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentHistoryDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * 결제 이력 엔티티
 * 
 * <p>
 * 결제의 모든 상태 변경 및 주요 이벤트를 기록한다.
 * 감사(Audit) 및 장애 발생 시 추적 용도로 사용된다.
 */
@Entity
@Table(name = "payment_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PaymentHistory extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payments_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentHistoryType type; // 기록 유형

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status_origin")
    private PaymentStatus paymentStatusOrigin; // 변경 전 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status_changed")
    private PaymentStatus paymentStatusChanged; // 변경 후 상태

    @Column(name = "amount_pg_origin")
    private Integer amountPgOrigin; // 변경 전 PG 승인액

    @Column(name = "amount_pg_changed")
    private Integer amountPgChanged; // 변경 후 PG 승인액

    @Column(name = "payment_deposit_origin")
    private Integer paymentDepositOrigin; // 변경 전 예치금 사용액

    @Column(name = "payment_deposit_changed")
    private Integer paymentDepositChanged; // 변경 후 예치금 사용액

    public static PaymentHistory create(Payment payment, PaymentHistoryType type,
            PaymentStatus paymentStatusOrigin, PaymentStatus paymentStatusChanged,
            Integer amountPgOrigin, Integer amountPgChanged,
            Integer paymentDepositOrigin, Integer paymentDepositChanged) {
        return PaymentHistory.builder()
                .payment(payment)
                .type(type)
                .paymentStatusOrigin(paymentStatusOrigin)
                .paymentStatusChanged(paymentStatusChanged)
                .amountPgOrigin(amountPgOrigin)
                .amountPgChanged(amountPgChanged)
                .paymentDepositOrigin(paymentDepositOrigin)
                .paymentDepositChanged(paymentDepositChanged)
                .build();
    }

    // === DTO 변환 ===

    public PaymentHistoryDto toDto() {
        return PaymentHistoryDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .paymentsId(this.payment.getId())
                .type(this.type)
                .paymentStatusOrigin(this.paymentStatusOrigin)
                .paymentStatusChanged(this.paymentStatusChanged)
                .amountPgOrigin(this.amountPgOrigin)
                .amountPgChanged(this.amountPgChanged)
                .paymentDepositOrigin(this.paymentDepositOrigin)
                .paymentDepositChanged(this.paymentDepositChanged)
                .createdAt(this.getCreatedAt())
                .build();
    }
}
