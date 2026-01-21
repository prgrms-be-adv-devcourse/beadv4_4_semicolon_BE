package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.Deposit;
import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.out.DepositHistoryRepository;
import dukku.semicolon.boundedContext.deposit.out.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DepositSupport {

    private final DepositRepository depositRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    public Optional<Deposit> findByUserUuid(UUID userUuid) {
        return depositRepository.findByUserUuid(userUuid);
    }

    public Deposit save(Deposit deposit) {
        return depositRepository.save(deposit);
    }

    public DepositHistory saveHistory(DepositHistory history) {
        return depositHistoryRepository.save(history);
    }

    public List<DepositHistory> findHistoriesByUserUuid(UUID userUuid) {
        return depositHistoryRepository.findByUserUuidOrderByCreatedAtDesc(userUuid);
    }

    public List<DepositHistory> findAllHistories() {
        return depositHistoryRepository.findAllByOrderByCreatedAtDesc();
    }
}
