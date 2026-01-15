package dukku.user.boundedContext.user.app;

import dukku.user.shared.user.dto.UserRegisterRequest;
import dukku.user.boundedContext.user.entity.User;
import dukku.user.boundedContext.user.entity.type.Role;
import dukku.user.boundedContext.user.entity.type.UserStatus;
import dukku.user.shared.user.exception.UserConflictException;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.user.shared.user.event.UserJoinedEvent;
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

