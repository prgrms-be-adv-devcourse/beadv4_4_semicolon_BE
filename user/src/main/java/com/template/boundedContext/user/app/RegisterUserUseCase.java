package com.template.boundedContext.user.app;

import com.template.shared.user.dto.UserRegisterRequest;
import com.template.boundedContext.user.entity.User;
import com.template.boundedContext.user.entity.type.Role;
import com.template.boundedContext.user.entity.type.UserStatus;
import com.template.shared.user.exception.UserConflictException;
import com.template.global.eventPublisher.EventPublisher;
import com.template.shared.user.event.UserJoinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserSupport support;
    private final EventPublisher eventPublisher;

    public User execute(UserRegisterRequest req, Role role) {
        User user_ = support.findByEmail(req.getEmail())
                .map(existing -> restoreOrFail(existing, req))
                .orElseGet(() -> createNew(req, role));

        User user = support.save(user_);
        eventPublisher.publish(new UserJoinedEvent(User.toUserDto(user)));

        return user;
    }

    private User restoreOrFail(User user, UserRegisterRequest req) {
        if (user.getDeletedAt() != null) {
            user.updateStatus(UserStatus.ACTIVE);
            user.updatePassword(support.encode(req.getPassword()));
            return user;
        }
        throw new UserConflictException();
    }

    private User createNew(UserRegisterRequest req, Role role) {
        req.setPassword(support.encode(req.getPassword()));
        return User.createUser(req, role);
    }
}

