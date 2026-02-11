package dukku.deposit.boundedContext.deposit.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.deposit.event.DepositChargeFailedEvent;
import dukku.common.shared.deposit.event.DepositChargeSucceededEvent;
import dukku.common.shared.deposit.type.DepositHistoryType;
import dukku.deposit.global.SystemDepositInitData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Deprecated : API 호출 방식으로 변경됨, 관련 로직 완전히 제거된 뒤 삭제 예정
 * 정산에 의한 예치금 충전 UseCase (Saga 참여, 이벤트 기반)
 *
 * <p>
 * 정산 완료 시 예치금을 충전하고 성공/실패 이벤트를 발행한다.
 *
 * @deprecated 멱등성 검증이 강화된 {@link ChargeDepositForSettlementUseCase} 사용을 권장합니다.
 */
@Deprecated
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeDepositUseCase {

    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final DecreaseDepositUseCase decreaseDepositUseCase;
    private final EventPublisher eventPublisher;

    /**
     * 정산 예치금 충전 실행
     *
     * @param userUuid       사용자 UUID
     * @param amount         충전 금액
     * @param settlementUuid 정산 UUID
     * @deprecated {@link ChargeDepositForSettlementUseCase#execute(UUID, Long, UUID)}
     *             사용을 권장합니다.
     */
    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(UUID userUuid, Long amount, UUID settlementUuid) {
        log.warn(
                "[DEPRECATED] ChargeDepositUseCase.execute()가 호출되었습니다. ChargeDepositForSettlementUseCase 사용으로 전환이 필요합니다. settlementUuid={}",
                settlementUuid);
        if (amount == null || amount <= 0) {
            return;
        }

        try {
            // 정산에 의한 충전은 SETTLEMENT 타입 사용
            increaseDepositUseCase.increase(userUuid, amount, DepositHistoryType.SETTLEMENT, settlementUuid);
            decreaseDepositUseCase.decrease(
                    SystemDepositInitData.SYSTEM_USER_UUID,
                    amount,
                    DepositHistoryType.SETTLEMENT,
                    settlementUuid);

            // 성공 이벤트 발행
            eventPublisher.publish(new DepositChargeSucceededEvent(userUuid, amount, settlementUuid));

        } catch (Exception e) {
            log.error("[정산 예치금 충전 실패] userUuid={}, amount={}, settlementUuid={}", userUuid, amount, settlementUuid, e);

            // 실패 이벤트 발행
            eventPublisher.publish(new DepositChargeFailedEvent(userUuid, amount, settlementUuid, e.getMessage()));
        }
    }
}
