package dukku.semicolon.boundedContext.user.app.user;

import dukku.semicolon.shared.user.dto.PasswordUpdateRequest;
import dukku.semicolon.shared.user.dto.UserRegisterRequest;
import dukku.semicolon.shared.user.dto.UserResponse;
import dukku.semicolon.shared.user.dto.UserUpdateRequest;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFacade {

    private final RegisterUserUseCase registerUser;
    private final FindUserUseCase findUser;
    private final UpdateUserUseCase updateUser;
    private final ChangePasswordUseCase changePassword;
    private final WithdrawUserUseCase withdrawUserUseCase;
    private final FindFollowingUsersUseCase findFollowingUsers;
    private final FindFollowerUsersUseCase findFollowerUsers;

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

    public void withdraw(UUID userUuid) {
        withdrawUserUseCase.withdraw(userUuid);
    }

    public List<UserResponse> findMyFollowings(UUID userUuid) {
        return findFollowingUsers.execute(userUuid)
                .stream()
                .map(User::toUserResponse)
                .toList();
    }

    public List<UserResponse> findMyFollowers(UUID userUuid) {
        return findFollowerUsers.execute(userUuid)
                .stream()
                .map(User::toUserResponse)
                .toList();
    }
}
