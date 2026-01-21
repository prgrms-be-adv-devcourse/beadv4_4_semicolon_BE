package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 예치금 내역 조회 UseCase
 *
 * <p>
 * 사용자 예치금 변동 내역 목록 조회 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindDepositHistoriesUseCase {

    private final DepositSupport depositSupport;

    /**
     * 특정 사용자 예치금 내역 조회
     */
    public List<DepositHistory> findHistories(UUID userUuid) {
        return depositSupport.findHistoriesByUserUuid(userUuid);
    }

    /**
     * 전체 사용자 예치금 내역 조회
     */
    public List<DepositHistory> findAllHistories() {
        return depositSupport.findAllHistories();
    }
}
