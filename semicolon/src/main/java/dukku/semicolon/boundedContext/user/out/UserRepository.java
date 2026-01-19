package dukku.semicolon.boundedContext.user.out;

import dukku.semicolon.boundedContext.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByUuidAndDeletedAtIsNull(UUID userUuid);
}