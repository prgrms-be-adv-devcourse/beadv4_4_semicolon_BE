package com.template.boundedContext.user.app;

import com.template.shared.user.dto.PasswordUpdateRequest;
import com.template.shared.user.dto.UserRegisterRequest;
import com.template.shared.user.dto.UserResponse;
import com.template.shared.user.dto.UserUpdateRequest;
import com.template.boundedContext.user.entity.User;
import com.template.boundedContext.user.entity.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFacade {

    private final RegisterUserUseCase registerUser;
    private final FindUserUseCase findUser;
    private final UpdateUserUseCase updateUser;
    private final ChangePasswordUseCase changePassword;

    public UserResponse registerUser(UserRegisterRequest req, Role role) {
        return User.toUserResponse(registerUser.execute(req, role));
    }

    @Transactional(readOnly = true)
    public UserResponse findUserByUserUuid(UUID userUuid) {
        return User.toUserResponse(findUser.execute(userUuid));
    }

    public UserResponse updateUserByUserUuid(UserUpdateRequest req, UUID userUuid) {
        return User.toUserResponse(updateUser.execute(req, userUuid));
    }

    public void updateUserPassword(PasswordUpdateRequest req) {
        changePassword.execute(req);
    }
}
