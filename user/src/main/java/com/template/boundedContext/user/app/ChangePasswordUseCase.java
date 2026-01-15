package com.template.boundedContext.user.app;

import com.template.global.eventPublisher.EventPublisher;
import com.template.shared.user.dto.PasswordUpdateRequest;
import com.template.boundedContext.user.entity.User;
import com.template.shared.user.event.UserModifiedEvent;
import com.template.shared.user.exception.UserNotFoundException;
import com.template.boundedContext.user.out.UserRepository;
import com.template.global.UserUtil;
import com.template.global.exception.UnauthorizedException;
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
