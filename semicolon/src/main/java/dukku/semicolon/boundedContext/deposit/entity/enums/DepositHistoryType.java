package dukku.semicolon.boundedContext.deposit.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예치금 변동 유형
 */
public enum DepositHistoryType {
    CHARGE, // 충전
    USE, // 사용
    REFUND, // 환불
    WITHDRAW, // 출금
    ADJUST // 조정
}
