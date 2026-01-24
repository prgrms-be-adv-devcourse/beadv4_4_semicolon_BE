package dukku.semicolon.shared.product.dto.cqrs;

import java.util.List;

public record ProductStatsBulkUpdatedEvent(
        List<ProductStatDto> updateBatchList // 변경된 통계 정보 리스트
) {}