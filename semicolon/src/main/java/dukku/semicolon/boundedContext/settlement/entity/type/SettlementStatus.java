package dukku.semicolon.boundedContext.settlement.entity.type;

import lombok.Getter;
/**
 * 정산 상태
 * - 상태 전이 규칙을 Enum 내부에서 관리
 */
@Getter
public enum SettlementStatus {
    CREATED("생성됨"),
    PENDING("대기중"),
    PROCESSING("처리중"),
    SUCCESS("정산완료"),
    FAILED("실패");

    private final String status;

    SettlementStatus(String status) {
        this.status = status;
    }

    public boolean canTransitTo(SettlementStatus next) {
        return switch (this) {
            case CREATED -> next == PENDING;
            case PENDING -> next == PROCESSING;
            case PROCESSING -> next == SUCCESS || next == FAILED;
            case SUCCESS -> false;
            case FAILED -> next == PENDING;
        };
    }
}
