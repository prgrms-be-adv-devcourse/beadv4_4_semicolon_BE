package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.shared.settlement.dto.SettlementSearchCondition;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetSettlementListUseCase {

    private final SettlementRepository settlementRepository;

    /**
     * 정산 목록 조회 (검색 조건 + 페이징)
     */
    @Transactional(readOnly = true)
    public Page<Settlement> execute(SettlementSearchCondition condition, Pageable pageable) {
        return settlementRepository.search(condition, pageable);
    }
}
