package com.template.shared.user.dto;

import com.template.boundedContext.user.entity.type.Role;
import com.template.boundedContext.user.entity.type.UserStatus;

import java.util.UUID;

public record UserDto(
        UUID userUuid,
        String email,
        String nickname,
        Role role,
        UserStatus status
) { }