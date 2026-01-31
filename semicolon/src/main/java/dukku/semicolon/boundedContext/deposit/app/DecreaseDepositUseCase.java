package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.Deposit;
import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 예치금 차감 UseCase
 *
 * <p>
 * 상품 구매, 취소/롤백 등으로 인한 잔액 차감 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class DecreaseDepositUseCase {

    private final FindDepositUseCase findDepositUseCase;
    private final DepositSupport depositSupport;

    /**
     * 예치금 차감
     *
     * <p>
     * 사용자의 예치금 잔액을 차감하고, 변동 내역(History)을 저장한다.
     * 잔액 부족 시
     * {@link dukku.semicolon.boundedContext.deposit.exception.NotEnoughDepositException}
     * 발생.
     * 
     * TODO: Phase 2에서 비관적 락 적용 예정
     */
    @Transactional
    public void decrease(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        Deposit deposit = findDepositUseCase.findOrCreate(userUuid);
        deposit.subtractBalance(amount);

        DepositHistory history = DepositHistory.create(
                userUuid,
                amount, // 절댓값으로 기록 (Type으로 구분)
                deposit.getBalance(),
                type,
                orderItemUuid);
        depositSupport.saveHistory(history);
    }
}
