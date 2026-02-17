package dukku.common.global.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "custom.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisFixedWindowRateLimiter {
    private static final String KEY_PREFIX = "rate_limit";

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitDecision tryConsume(String policyName, String identifier, long limit, long windowSeconds) {
        long safeLimit = Math.max(limit, 1L);
        long safeWindow = Math.max(windowSeconds, 1L);

        long nowEpochSeconds = Instant.now().getEpochSecond();
        long windowStart = (nowEpochSeconds / safeWindow) * safeWindow;
        String redisKey = KEY_PREFIX + ":" + policyName + ":" + identifier + ":" + windowStart;

        Long current = stringRedisTemplate.opsForValue().increment(redisKey);
        if (current != null && current == 1L) {
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(safeWindow + 1L));
        }

        long used = current == null ? safeLimit : current;
        boolean allowed = used <= safeLimit;
        long remaining = Math.max(safeLimit - used, 0L);
        long retryAfter = safeWindow - (nowEpochSeconds % safeWindow);
        if (retryAfter <= 0L) {
            retryAfter = 1L;
        }

        return new RateLimitDecision(allowed, safeLimit, remaining, retryAfter);
    }
}
