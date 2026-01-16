package dukku.user.shared.user.event;

import dukku.user.shared.user.dto.UserDto;

public record UserJoinedEvent(UserDto member) {
}
