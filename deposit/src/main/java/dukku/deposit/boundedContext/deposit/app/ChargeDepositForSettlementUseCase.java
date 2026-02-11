package dukku.deposit.boundedContext.deposit.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.deposit.event.DepositChargeFailedEvent;
import dukku.common.shared.deposit.event.DepositChargeSucceededEvent;
import dukku.common.shared.deposit.type.DepositHistoryType;
import dukku.common.shared.deposit.dto.DepositChargeForSettlementResponse;
import dukku.common.shared.deposit.dto.DepositDto;
import dukku.common.shared.deposit.type.DepositChargeResultCode;
import dukku.deposit.global.SystemDepositInitData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 정산을 위한 예치금 충전 UseCase
 *
 * <p>
 * Internal API에서 호출되는 정산 예치금 충전 로직을 담당합니다.
 * settlementUuid를 멱등키로 사용하여 중복 충전을 방지합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeDepositForSettlementUseCase {

    private final FindDepositUseCase findDepositUseCase;
    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final DecreaseDepositUseCase decreaseDepositUseCase;
    private final FindDepositHistoriesUseCase findDepositHistoriesUseCase;
    private final EventPublisher eventPublisher;

    /**
     * 정산 예치금 충전 실행
     *
     * @param userUuid       충전 대상 사용자 UUID
     * @param amount         충전 금액
     * @param settlementUuid 정산 UUID (멱등키)
     * @return 충전 결과 응답 DTO
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DepositChargeForSettlementResponse execute(UUID userUuid, Long amount, UUID settlementUuid) {
        if (amount == null || amount <= 0) {
            DepositChargeResultCode resultCode = DepositChargeResultCode.INVALID_AMOUNT;
            return DepositChargeForSettlementResponse.failure(
                    resultCode,
                    resultCode.getMessage());
        }

        try {
            boolean alreadyCharged = findDepositHistoriesUseCase.existsBySettlementUuid(settlementUuid);
            if (alreadyCharged) {
                log.info("[Internal API] 이미 처리된 정산 충전 요청. settlementUuid={}", settlementUuid);
                DepositDto deposit = findDepositUseCase.findOrCreate(userUuid).toDto();
                return DepositChargeForSettlementResponse.success(
                        deposit.getDepositUuid(), amount, deposit.getBalance());
            }

            increaseDepositUseCase.increase(userUuid, amount, DepositHistoryType.SETTLEMENT, settlementUuid);
            decreaseDepositUseCase.decrease(
                    SystemDepositInitData.SYSTEM_USER_UUID,
                    amount,
                    DepositHistoryType.SETTLEMENT,
                    settlementUuid);
            eventPublisher.publish(new DepositChargeSucceededEvent(userUuid, amount, settlementUuid));

            DepositDto deposit = findDepositUseCase.findOrCreate(userUuid).toDto();
            log.info("[Internal API] 정산 예치금 충전 성공. userUuid={}, amount={}, settlementUuid={}",
                    userUuid, amount, settlementUuid);

            return DepositChargeForSettlementResponse.success(
                    deposit.getDepositUuid(), amount, deposit.getBalance());

        } catch (Exception e) {
            log.error("[Internal API] 정산 예치금 충전 실패. userUuid={}, amount={}, settlementUuid={}",
                    userUuid, amount, settlementUuid, e);

            eventPublisher.publish(new DepositChargeFailedEvent(userUuid, amount, settlementUuid, e.getMessage()));

            DepositChargeResultCode resultCode = DepositChargeResultCode.CHARGE_FAILED;
            return DepositChargeForSettlementResponse.failure(
                    resultCode,
                    resultCode.getMessage() + ": " + e.getMessage());
        }
    }
}
