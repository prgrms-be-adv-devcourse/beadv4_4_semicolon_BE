package dukku.semicolon.boundedContext.user.in;

import dukku.semicolon.boundedContext.user.app.user.UserFacade;
import dukku.semicolon.shared.user.dto.PasswordUpdateRequest;
import dukku.semicolon.shared.user.dto.UserRegisterRequest;
import dukku.semicolon.shared.user.dto.UserResponse;
import dukku.semicolon.shared.user.dto.UserUpdateRequest;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.common.global.UserUtil;
import dukku.semicolon.shared.user.docs.UserApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@UserApiDocs.UserTag
public class UserController {
    private final UserFacade userFacade;

    @PostMapping("/register")
    @UserApiDocs.RegisterUser
    public ResponseEntity<UserResponse> registerUser(
            @RequestBody @Validated UserRegisterRequest userRegisterRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userFacade.registerUser(userRegisterRequest, Role.USER));
    }

    @GetMapping("/me")
    @UserApiDocs.GetMe
    public UserResponse findUserByUserId() {
        return userFacade.findUserByUserUuid(UserUtil.getUserId());
    }

    @PutMapping("/me")
    @UserApiDocs.UpdateMe
    public UserResponse updateUser(
            @RequestBody @Validated UserUpdateRequest userUpdateRequest
    ) {
        return userFacade.updateUserByUserUuid(userUpdateRequest, UserUtil.getUserId());
    }

    @PutMapping("/password")
    @UserApiDocs.UpdatePassword
    public ResponseEntity<Void> updateUserPassword(
            @RequestBody @Validated PasswordUpdateRequest passwordUpdateRequest
    ) {
        userFacade.updateUserPassword(passwordUpdateRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @UserApiDocs.deleteUser
    public ResponseEntity<Void> deleteUser() {
        userFacade.withdraw(UserUtil.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/followings")
    public List<UserResponse> myFollowings() {
        // 기존 메서드들과 동일하게 UserUtil을 사용합니다.
        return userFacade.findMyFollowings(UserUtil.getUserId());
    }

    @GetMapping("/me/followers")
    public List<UserResponse> myFollowers() {
        return userFacade.findMyFollowers(UserUtil.getUserId());
    }

}