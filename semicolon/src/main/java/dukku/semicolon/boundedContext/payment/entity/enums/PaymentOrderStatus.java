package dukku.semicolon.boundedContext.payment.entity.enums;

/**
 * 결제 주문 상태
 */
public enum PaymentOrderStatus {
    PAID, // 결제 완료
    PAYMENT_FAILED, // 결제 실패
    CANCELED, // 취소됨
    PARTIAL_REFUNDED // 부분 환불됨
}
