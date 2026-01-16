package dukku.semicolon.boundedContext.payment.entity.enums;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING, // 승인 대기
    DONE, // 완료
    FAILED, // 실패
    CANCELED, // 전액 취소
    PARTIAL_CANCELED // 부분 취소
}
