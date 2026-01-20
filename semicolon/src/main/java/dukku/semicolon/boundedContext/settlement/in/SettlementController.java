package dukku.semicolon.boundedContext.settlement.in;

import dukku.semicolon.boundedContext.settlement.app.SettlementFacade;
import dukku.semicolon.shared.settlement.docs.SettlementApiDocs;
import dukku.semicolon.shared.settlement.dto.SettlementResponse;
import dukku.semicolon.shared.settlement.dto.SettlementSearchRequest;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsRequest;
import dukku.semicolon.shared.settlement.dto.SettlementStatisticsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/settlements")
@RequiredArgsConstructor
@SettlementApiDocs.SettlementTag
public class SettlementController {

    private final SettlementFacade settlementFacade;


    /**
     * 정산 목록 조회
     * GET /admin/settlements
     */
    @GetMapping
    @SettlementApiDocs.GetSettlements
    public Page<SettlementResponse> getSettlements(
            @Valid @ModelAttribute SettlementSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return settlementFacade.getSettlements(request.toCondition(), pageable);
    }

    /**
     * 정산 통계 조회
     * GET /admin/settlements/statistics
     */
    @GetMapping("/statistics")
    @SettlementApiDocs.GetSettlementStatistics
    public SettlementStatisticsResponse getStatistics(
            @Valid @ModelAttribute SettlementStatisticsRequest request
    ) {
        return settlementFacade.getStatistics(request.toCondition());
    }
}
