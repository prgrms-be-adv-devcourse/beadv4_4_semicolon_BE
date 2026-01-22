package dukku.semicolon.boundedContext.user.out;

import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    // 내가 팔로우한 사용자들
    List<UserFollow> findByFollowerId(UUID followerId);

    // 나를 팔로우한 사용자들
    List<UserFollow> findByFollowingId(UUID followingId);

}
