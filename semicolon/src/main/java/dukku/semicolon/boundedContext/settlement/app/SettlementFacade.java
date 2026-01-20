package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.shared.settlement.dto.SettlementResponse;
import dukku.semicolon.shared.settlement.dto.SettlementSearchCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsCondition;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsResponse;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementFacade {

    private final GetSettlementListUseCase getSettlements;
    private final GetSettlementStatisticsUseCase getStatistics;


    @Transactional(readOnly = true)
    public Page<SettlementResponse> getSettlements(SettlementSearchCondition condition, Pageable pageable) {
        Page<Settlement> settlements = getSettlements.execute(condition, pageable);
        return settlements.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SettlementStatisticsResponse getStatistics(SettlementStatisticsCondition condition) {
        return getStatistics.execute(condition);
    }

    /**
     * TODO: 외부 서비스 호출로 sellerNickname, productName, bankName, accountNumber 조회
     */
    private SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.of(
                settlement,
                null,  // sellerNickname - 추후 외부 서비스 연동
                null,  // productName - 추후 외부 서비스 연동
                null,  // bankName - 추후 외부 서비스 연동
                null   // accountNumber - 추후 외부 서비스 연동
        );
    }
}
