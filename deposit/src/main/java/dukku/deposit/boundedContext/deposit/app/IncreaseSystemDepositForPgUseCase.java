package dukku.deposit.boundedContext.deposit.app;

import dukku.common.shared.deposit.type.DepositHistoryType;
import dukku.deposit.global.SystemDepositInitData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * PG 결제 승인분을 시스템 지갑에 반영하는 UseCase
 */
@Service
@RequiredArgsConstructor
public class IncreaseSystemDepositForPgUseCase {

    private final IncreaseDepositUseCase increaseDepositUseCase;

    @Transactional
    public void execute(UUID orderUuid, Long pgAmount) {
        if (pgAmount == null || pgAmount <= 0) {
            return;
        }

        increaseDepositUseCase.increase(
                SystemDepositInitData.SYSTEM_USER_UUID,
                pgAmount,
                DepositHistoryType.PG_CHARGE,
                orderUuid);
    }
}
