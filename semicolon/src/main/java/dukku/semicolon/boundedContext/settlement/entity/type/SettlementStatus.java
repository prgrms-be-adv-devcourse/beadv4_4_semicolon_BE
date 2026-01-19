package dukku.semicolon.boundedContext.settlement.entity.type;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    CREATED("생성됨"),
    PROCESSING("처리중"),
    PENDING("대기중"),
    SUCCESS("정산완료"),
    FAILED("실패");

    private final String label;

    SettlementStatus(String label) {
        this.label = label;
    }
}
