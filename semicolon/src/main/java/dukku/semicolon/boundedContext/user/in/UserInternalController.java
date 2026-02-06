package dukku.semicolon.boundedContext.user.in;

import dukku.common.global.exception.BadRequestException;
import dukku.semicolon.boundedContext.user.app.user.FindUserByEmailUseCase;
import dukku.semicolon.boundedContext.user.app.user.FindUserByRoleUseCase;
import dukku.semicolon.boundedContext.user.app.user.FindUserUseCase;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.shared.user.dto.UserNicknameResponse;
import dukku.semicolon.shared.user.dto.UserUuidResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Hidden
public class UserInternalController {

    private final FindUserByRoleUseCase findUserByRole;
    private final FindUserByEmailUseCase findUserByEmail;
    private final FindUserUseCase findUser;

    @GetMapping("/uuid")
    public ResponseEntity<UserUuidResponse> getUserUuid(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String email
    ) {
        if ((role == null) == (email == null)) {
            throw new BadRequestException("Provide exactly one of role or email.");
        }
        if (email != null && email.trim().isEmpty()) {
            throw new BadRequestException("Email must not be blank.");
        }

        User user = role != null
                ? findUserByRole.execute(role)
                : findUserByEmail.execute(email.trim());

        log.info("[Internal API] User UUID lookup. role={}, email={}", role, email);

        UserUuidResponse response = UserUuidResponse.builder()
                .userUuid(user.getUuid())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userUuid}/nickname")
    public ResponseEntity<UserNicknameResponse> getUserNickname(@PathVariable UUID userUuid) {
        User user = findUser.execute(userUuid);

        log.info("[Internal API] User nickname lookup. userUuid={}", userUuid);

        UserNicknameResponse response = UserNicknameResponse.builder()
                .userUuid(user.getUuid())
                .nickname(user.getNickname())
                .build();

        return ResponseEntity.ok(response);
    }
}
