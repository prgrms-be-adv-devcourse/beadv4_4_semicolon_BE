package dukku.common.shared.payment.type;

/**
 * 결제 이력 유형
 */
public enum PaymentHistoryType {
    // 결제
    PAYMENT_REQUESTED, // 결제 요청 (준비)
    PAYMENT_SUCCESS, // 결제 성공
    PAYMENT_FAILED, // 결제 실패

    // 주문 취소
    ORDER_CANCEL_SUCCESS, // 주문 취소 성공
    ORDER_CANCEL_FAILED, // 주문 취소 실패

    // 부분 환불
    PARTIAL_REFUND_SUCCESS, // 부분 환불 성공
    PARTIAL_REFUND_FAILED, // 부분 환불 실패

    // 전체 환불
    FULL_REFUND_SUCCESS, // 전체 환불 성공
    FULL_REFUND_FAILED, // 전체 환불 실패

    // 기타
    PAYMENT_ROLLBACK_FAILED // 보상 트랜잭션 실패
}
