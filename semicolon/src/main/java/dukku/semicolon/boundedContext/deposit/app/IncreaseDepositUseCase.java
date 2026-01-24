package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.Deposit;
import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 예치금 증가 UseCase
 *
 * <p>
 * 예치금 충전, 정산, 환불 등으로 인한 잔액 증가 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class IncreaseDepositUseCase {

    private final FindDepositUseCase findDepositUseCase;
    private final DepositSupport depositSupport;

    /**
     * 예치금 증가
     *
     * <p>
     * 사용자의 예치금 잔액을 증가시키고, 변동 내역(History)을 저장한다.
     */
    @Transactional
    public void increase(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        Deposit deposit = findDepositUseCase.findOrCreate(userUuid);
        deposit.addBalance(amount);

        DepositHistory history = DepositHistory.create(
                userUuid,
                amount,
                deposit.getBalance(),
                type,
                orderItemUuid);
        depositSupport.saveHistory(history);
    }
}
