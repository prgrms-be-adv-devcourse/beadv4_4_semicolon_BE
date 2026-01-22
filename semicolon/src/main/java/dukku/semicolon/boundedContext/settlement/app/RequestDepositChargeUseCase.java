package dukku.semicolon.boundedContext.settlement.app;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 예치금 충전 요청 UseCase
 * - Settlement의 정산금액을 판매자 예치금에 충전 요청
 * - Settlement 상태: PENDING → PROCESSING
 * - Deposit BC에서 충전 완료/실패 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestDepositChargeUseCase {

    public Settlement execute(Settlement settlement) {
        // TODO: Deposit BC에 충전 요청
        return settlement;
    }
}
