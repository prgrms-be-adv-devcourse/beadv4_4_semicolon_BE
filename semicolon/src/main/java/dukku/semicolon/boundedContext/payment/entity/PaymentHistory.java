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

/**
 * 결제 이력 엔티티
 * 
 * <p>
 * 결제의 모든 상태 변경 및 주요 이벤트를 기록한다.
 * 감사(Audit) 및 장애 발생 시 추적 용도로 사용된다.
 */
@Entity
@Table(name = "payment_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PaymentHistory extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "기록 유형")
    private PaymentHistoryType type;

    @Enumerated(EnumType.STRING)
    @Column(comment = "변경 전 상태")
    private PaymentStatus paymentStatusOrigin;

    @Enumerated(EnumType.STRING)
    @Column(comment = "변경 후 상태")
    private PaymentStatus paymentStatusChanged;

    @Column(comment = "변경 전 PG 승인액")
    private Integer amountPgOrigin;

    @Column(comment = "변경 후 PG 승인액")
    private Integer amountPgChanged;

    @Column(comment = "변경 전 예치금 사용액")
    private Integer paymentDepositOrigin;

    @Column(comment = "변경 후 예치금 사용액")
    private Integer paymentDepositChanged;

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
                .paymentId(this.payment.getId())
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
