package dukku.semicolon.boundedContext.user.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.shared.user.dto.UserUpdateRequest;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.shared.user.event.UserModifiedEvent;
import dukku.semicolon.shared.user.exception.UserNotFoundException;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public User execute(UserUpdateRequest request, UUID userUuid) {
        User user = userRepository.findByUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(UserNotFoundException::new);

        user.updateUser(request);

        eventPublisher.publish(new UserModifiedEvent(User.toUserDto(user)));
        
        return user;
    }
}

