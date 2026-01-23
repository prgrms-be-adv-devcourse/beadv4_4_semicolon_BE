package dukku.semicolon.boundedContext.settlement.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.settlement.event.SettlementDepositChargeRequestedEvent;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    private final SettlementSupport settlementSupport;
    private final EventPublisher eventPublisher;

    @Transactional
    public Settlement execute(Settlement settlement) {
        log.info("[예치금 충전 요청] settlementUuid={}, sellerUuid={}, amount={}",
                settlement.getUuid(), settlement.getSellerUuid(), settlement.getSettlementAmount());

        // 1. Settlement 상태를 PROCESSING으로 변경
        settlement.startProcessing();
        settlementSupport.save(settlement);

        // 2. 예치금 충전 요청 이벤트 발행 (Deposit BC로)
        eventPublisher.publish(
                new SettlementDepositChargeRequestedEvent(
                        settlement.getSellerUuid(),       // 판매자 UUID
                        settlement.getSettlementAmount(), // 정산 금액 (수수료 제외)
                        settlement.getUuid()              // 정산 UUID
                )
        );

        log.info("[예치금 충전 요청 이벤트 발행] settlementUuid={}, amount={}",
                settlement.getUuid(), settlement.getSettlementAmount());

        return settlement;
    }
}
