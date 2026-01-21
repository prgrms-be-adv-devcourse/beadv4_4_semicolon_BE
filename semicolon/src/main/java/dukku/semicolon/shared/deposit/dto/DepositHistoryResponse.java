package dukku.semicolon.shared.deposit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 예치금 내역 조회 응답 DTO (페이징 포함)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositHistoryResponse {
    private boolean success;
    private String code;
    private String message;
    private DepositHistoryData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepositHistoryData {
        private List<DepositHistoryHistoryItem> items;
        private PageInfo page;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepositHistoryHistoryItem {
        private String depositHistoryId; // UUID 형태의 String (이력 ID)
        private String type; // 변동 유형 (Enum String)
        private Long amount; // 변동 금액
        private Long balanceAfter; // 변동 후 잔액
        private ReferenceInfo ref; // 참조 정보
        private OffsetDateTime createdAt; // 생성 일시
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReferenceInfo {
        private UUID paymentId;
        private UUID orderUuid;
        private UUID settlementUuid;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageInfo {
        private Integer size;
        private String nextCursor;
    }
}
