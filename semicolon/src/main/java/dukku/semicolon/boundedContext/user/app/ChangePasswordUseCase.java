package dukku.semicolon.boundedContext.user.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.shared.user.dto.PasswordUpdateRequest;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.shared.user.event.UserModifiedEvent;
import dukku.semicolon.shared.user.exception.UserNotFoundException;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import dukku.common.global.UserUtil;
import dukku.common.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(PasswordUpdateRequest request) {
        UUID currentUserId = UserUtil.getUserId();

        User user = userRepository.findByUuidAndDeletedAtIsNull(currentUserId)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호 불일치");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        eventPublisher.publish(new UserModifiedEvent(User.toUserDto(user)));
    }
}
