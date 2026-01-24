package dukku.common.shared.payment.type;

/**
 * 결제 유형
 */
public enum PaymentType {
    NORMAL, // 일반 결제
    DEPOSIT, // 예치금 전액 결제
    MIXED, // 혼합 결제 (PG + 예치금)
}
