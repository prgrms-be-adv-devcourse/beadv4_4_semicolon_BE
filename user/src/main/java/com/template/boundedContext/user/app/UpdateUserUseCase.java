package com.template.boundedContext.user.app;

import com.template.global.eventPublisher.EventPublisher;
import com.template.shared.user.dto.UserUpdateRequest;
import com.template.boundedContext.user.entity.User;
import com.template.shared.user.event.UserModifiedEvent;
import com.template.shared.user.exception.UserNotFoundException;
import com.template.boundedContext.user.out.UserRepository;
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

