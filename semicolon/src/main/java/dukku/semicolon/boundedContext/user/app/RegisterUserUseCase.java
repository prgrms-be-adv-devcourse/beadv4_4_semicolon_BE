package dukku.semicolon.boundedContext.user.app;

import dukku.semicolon.shared.user.dto.UserRegisterRequest;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.boundedContext.user.entity.type.UserStatus;
import dukku.semicolon.shared.user.exception.UserConflictException;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.shared.user.event.UserJoinedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final dukku.semicolon.boundedContext.user.app.UserSupport support;
    private final ApplicationEventPublisher springEventPublisher; // ✅ 이름 변경(충돌 제거)

    @Transactional
    public User execute(UserRegisterRequest req, Role role) {
        User userCandidate = support.findByEmail(req.getEmail())
                .map(existing -> restoreOrFail(existing, req))
                .orElseGet(() -> createNew(req, role));

        User saved = support.save(userCandidate);
        //회원가입 완료 스프링 이벤트 발행
        springEventPublisher.publishEvent(new UserJoinedEvent(User.toUserDto(saved)));
        return saved;
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
        String encoded = support.encode(req.getPassword());
        return User.createUser(req, role, encoded);
    }
}

