package dukku.semicolon.shared.user.event;

import dukku.semicolon.shared.user.dto.UserDto;

public record UserJoinedEvent(UserDto member) {
}
