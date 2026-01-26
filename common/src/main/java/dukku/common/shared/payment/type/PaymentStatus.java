package dukku.common.shared.payment.type;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING, // 승인 대기
    DONE, // 완료
    ABORTED, // 중단됨 (결제 흐름 중도 이탈)
    FAILED, // 실패
    CANCELED, // 전액 취소
    PARTIAL_CANCELED, // 부분 취소
    ROLLBACK_FAILED // 보상 트랜잭션 실패 (PG 취소 실패)
}
