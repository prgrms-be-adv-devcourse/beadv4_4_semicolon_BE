package dukku.semicolon.boundedContext.user.app.user;

import dukku.common.global.exception.UnauthorizedException;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserSupport {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }

    public User getActiveUserByUuid(UUID userUuid) {
        return repository.findByUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> new UnauthorizedException("존재하지 않거나 탈퇴한 사용자입니다."));
    }
}
