package dukku.user.shared.user.dto;

import dukku.user.boundedContext.user.entity.type.Role;
import dukku.user.boundedContext.user.entity.type.UserStatus;

import java.util.UUID;

public record UserDto(
        UUID userUuid,
        String email,
        String nickname,
        Role role,
        UserStatus status
) { }