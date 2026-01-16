package dukku.user.boundedContext.user.entity;

import dukku.user.shared.user.dto.UserRegisterRequest;
import dukku.user.shared.user.dto.UserResponse;
import dukku.user.shared.user.dto.UserUpdateRequest;
import dukku.user.boundedContext.user.entity.type.Role;
import dukku.user.boundedContext.user.entity.type.UserStatus;
import dukku.user.shared.user.domain.SourceUser;
import dukku.user.shared.user.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends SourceUser {
    @Column(length = 100, nullable = false, comment = "암호화된 비밀번호")
    private String password;

    public static User createUser(UserRegisterRequest req, Role role) {
        return User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .role(role)
                .nickname(req.getNickname())
                .build();
    }

    public void updateUser(UserUpdateRequest req) {
        this.setNickname(req.getName());
    }

    public void updateRole(Role role) {
        this.setRole(role);
    }

    public void updateStatus(UserStatus status) {
        this.setStatus(status);
    }

    public void deleteUser() {
        this.setDeletedAt(LocalDateTime.now());
        this.setStatus(UserStatus.DELETED);
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUuid(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus()
        );
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getUuid(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus()
        );
    }
}