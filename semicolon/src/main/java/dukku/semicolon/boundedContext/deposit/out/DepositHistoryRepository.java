package dukku.semicolon.boundedContext.deposit.out;

import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 예치금 이력 Repository
 */
public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Integer> {

    List<DepositHistory> findByUserUuidOrderByCreatedAtDesc(UUID userUuid);

    List<DepositHistory> findAllByOrderByCreatedAtDesc();

    List<DepositHistory> findByOrderItemUuid(UUID orderItemUuid);
}
