package dukku.common.shared.deposit.type;

/**
 * 예치금 변동 유형
 */
public enum DepositHistoryType {
    CHARGE, // 충전
    PG_CHARGE, // PG 결제 승인 입금
    DEPOSIT_CHARGE, // 예치금 사용 입금
    SETTLEMENT, // 정산
    USE, // 사용
    ROLLBACK, // 롤백 (정산 취소 등)
    REFUND, // 환불
    WITHDRAW, // 출금
    ADJUST // 조정
}
