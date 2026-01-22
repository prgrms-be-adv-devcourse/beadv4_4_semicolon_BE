package dukku.semicolon.boundedContext.settlement.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 이벤트 기반 Settlement 생성 UseCase
 * - OrderItemConfirmedEvent 수신 → Settlement 생성 (PENDING 상태)
 * - 배치에서 PENDING Settlement를 조회하여 예치금 충전
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateSettlementUseCase {


}
