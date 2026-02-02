package dukku.common.shared.payment.type;

/**
 * 결제 이력 유형
 */
public enum PaymentHistoryType {
    // 결제
    PAY_REQUESTED,        // 결제 요청 (준비)
    PAY_SUCCESS,          // 결제 성공
    PAY_FAILED,           // 결제 실패

    // 주문 취소
    CANCEL_SUCCESS,       // 주문 취소 성공
    CANCEL_FAILED,        // 주문 취소 실패

    // 부분 환불
    PARTIAL_REFUND_OK,    // 부분 환불 성공
    PARTIAL_REFUND_FAIL,  // 부분 환불 실패

    // 전체 환불
    FULL_REFUND_OK,       // 전체 환불 성공
    FULL_REFUND_FAIL,     // 전체 환불 실패

    // 기타
    ROLLBACK_FAILED       // 보상 트랜잭션 실패
}
