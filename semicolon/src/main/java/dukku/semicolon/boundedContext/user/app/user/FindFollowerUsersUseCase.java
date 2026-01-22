package dukku.semicolon.boundedContext.user.app.user;

import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.UserFollow;
import dukku.semicolon.boundedContext.user.out.UserFollowRepository;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFollowerUsersUseCase {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public List<User> execute(UUID userUuid) {
        List<UUID> followerIds = userFollowRepository.findByFollowingId(userUuid)
                .stream()
                .map(UserFollow::getFollowerId)
                .toList();

        return userRepository.findAllByUuidIn(followerIds);
    }
}
