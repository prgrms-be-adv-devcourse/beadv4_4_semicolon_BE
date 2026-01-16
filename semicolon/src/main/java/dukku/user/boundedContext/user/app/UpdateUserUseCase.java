package dukku.user.boundedContext.user.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.user.shared.user.dto.UserUpdateRequest;
import dukku.user.boundedContext.user.entity.User;
import dukku.user.shared.user.event.UserModifiedEvent;
import dukku.user.shared.user.exception.UserNotFoundException;
import dukku.user.boundedContext.user.out.UserRepository;
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

