package dukku.semicolon.boundedContext.user.app;

import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.shared.user.exception.UserNotFoundException;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(UUID userUuid) {
        return userRepository.findByUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(UserNotFoundException::new);
    }
}

