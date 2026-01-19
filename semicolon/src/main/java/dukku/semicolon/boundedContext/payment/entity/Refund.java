package dukku.semicolon.boundedContext.payment.entity;

import dukku.common.global.jpa.entity.BaseIdAndUUIDAndTime;
import dukku.semicolon.boundedContext.payment.entity.enums.RefundStatus;
import dukku.semicolon.shared.payment.dto.RefundDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 환불 정보 엔티티
 * 
 * <p>
 * 특정 {@code Payment}에 대한 환불 처리를 기록한다.
 * 총 환불 금액과 예치금으로 환불된 금액을 나누어 관리한다.
 * 
 * <h3>금액 필드 설명</h3>
 * <ul>
 * <li>{@code refundAmountTotal} - 총 환불 금액 (PG 취소액 + 예치금 환불액)</li>
 * <li>{@code refundDepositTotal} - 이 중 예치금으로 복구된 금액</li>
 * </ul>
 */
@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Refund extends BaseIdAndUUIDAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Payment payment;

    @Column(nullable = false, comment = "총 환불 금액")
    private Integer refundAmountTotal;

    @Column(nullable = false, comment = "예치금으로 복구된 금액")
    private Integer refundDepositTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "환불 상태")
    private RefundStatus refundStatus;

    @Column(comment = "환불 승인일")
    private LocalDateTime approvedAt;

    @Builder.Default
    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefundItem> items = new ArrayList<>();

    // === 정적 팩토리 메서드 ===

    public static Refund create(Payment payment, Integer amount, Integer depositAmount) {
        return Refund.builder()
                .payment(payment)
                .refundAmountTotal(amount)
                .refundDepositTotal(depositAmount)
                .refundStatus(RefundStatus.PENDING)
                .build();
    }

    // === 도메인 로직 ===

    public void complete() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.approvedAt = LocalDateTime.now();
    }

    public void addRefundItem(RefundItem item) {
        this.items.add(item);
    }

    // === DTO 변환 ===

    public RefundDto toDto() {
        return RefundDto.builder()
                .id(this.getId())
                .uuid(this.getUuid())
                .refundAmountTotal(this.refundAmountTotal)
                .refundDepositTotal(this.refundDepositTotal)
                .refundStatus(this.refundStatus)
                .approvedAt(this.approvedAt)
                .createdAt(this.getCreatedAt())
                .items(this.items.stream().map(RefundItem::toDto).toList())
                .build();
    }
}
