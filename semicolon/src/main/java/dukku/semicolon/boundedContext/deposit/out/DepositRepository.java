package dukku.semicolon.boundedContext.deposit.out;

import dukku.semicolon.boundedContext.deposit.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 예치금 Repository
 */
public interface DepositRepository extends JpaRepository<Deposit, UUID> {

    Optional<Deposit> findByUserUuid(UUID userUuid);

    Optional<Deposit> findByDepositUuid(UUID depositUuid);
}
