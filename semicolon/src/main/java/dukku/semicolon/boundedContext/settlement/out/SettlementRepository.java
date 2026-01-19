package dukku.semicolon.boundedContext.settlement.out;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, Long>{

    Optional<Settlement> findByUuid(UUID settlementUuid);

    // 판매자별 정산 목록 조회
    Page<Settlement> findBySellerUuid(UUID sellerUuid, Pageable pageable);

    // 상태별 정산 목록 조회
    Page<Settlement> findBySettlementStatus(SettlementStatus status, Pageable pageable);
}
