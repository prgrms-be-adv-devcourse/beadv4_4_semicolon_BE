package dukku.user.boundedContext.user.app;

import dukku.user.boundedContext.user.entity.User;
import dukku.user.shared.user.exception.UserNotFoundException;
import dukku.user.boundedContext.user.out.UserRepository;
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

