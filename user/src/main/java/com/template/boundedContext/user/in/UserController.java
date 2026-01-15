package com.template.boundedContext.user.in;

import com.template.boundedContext.user.app.UserFacade;
import com.template.shared.user.dto.PasswordUpdateRequest;
import com.template.shared.user.dto.UserRegisterRequest;
import com.template.shared.user.dto.UserResponse;
import com.template.shared.user.dto.UserUpdateRequest;
import com.template.boundedContext.user.entity.type.Role;
import com.template.global.UserUtil;
import com.template.shared.user.docs.UserApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}