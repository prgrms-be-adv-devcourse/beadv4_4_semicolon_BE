package dukku.common.shared.payment.type;

/**
 * 환불 상태
 */
public enum RefundStatus {
    PENDING, // 환불 대기
    COMPLETED, // 환불 완료
    CANCELED // 환불 취소
}
