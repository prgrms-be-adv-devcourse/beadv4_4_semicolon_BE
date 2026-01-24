package dukku.semicolon.boundedContext.deposit.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예치금 변동 유형
 */
public enum DepositHistoryType {
    CHARGE, // 충전
    SETTLEMENT, // 정산
    USE, // 사용
    ROLLBACK, // 롤백 (정산 취소 등)
    REFUND, // 환불
    WITHDRAW, // 출금
    ADJUST // 조정
}
