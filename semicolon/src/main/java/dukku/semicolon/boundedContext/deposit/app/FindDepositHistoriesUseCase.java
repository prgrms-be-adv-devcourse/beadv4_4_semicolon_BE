package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
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
     * 내역 목록 조회 (커서 기반 페이징)
     *
     * <p>
     * 사용자의 예치금 변동 내역을 최신순으로 정렬하여 반환한다.
     */
    public Slice<DepositHistory> findHistories(UUID userUuid, Integer cursor, int size) {
        return depositSupport.findHistoriesByCursor(userUuid, cursor, size);
    }

    /**
     * 전체 내역 목록 조회 (관리자용, 커서 기반 페이징)
     */
    public Slice<DepositHistory> findAllHistories(Integer cursor, int size) {
        return depositSupport.findHistoriesByCursor(null, cursor, size);
    }

    /**
     * @deprecated Use {@link #findHistories(UUID, Integer, int)} instead.
     */
    @Deprecated
    public List<DepositHistory> findHistories(UUID userUuid) {
        return depositSupport.findHistoriesByUserUuid(userUuid);
    }

    /**
     * @deprecated Use {@link #findAllHistories(Integer, int)} instead.
     */
    @Deprecated
    public List<DepositHistory> findAllHistories() {
        return depositSupport.findAllHistories();
    }
}
