package com.template.shared.user.event;

import com.template.shared.user.dto.UserDto;

public record UserJoinedEvent(UserDto member) {
}
