package dukku.semicolon.boundedContext.product.app.cqrs;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProductStatsRedisSupport {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key 상수 관리
    private static final String DIRTY_KEY = "product:stats:dirty";
    private static final String VIEW_KEY_PREFIX = "product:stats:view:";
    private static final String LIKE_KEY_PREFIX = "product:stats:like:";
    private static final String COMMENT_KEY_PREFIX = "product:stats:comment:";

    /** [Write] 숫자 증가/감소 및 Dirty Marking */
    public void incrementLike(Integer productId) {
        redisTemplate.opsForValue().increment(LIKE_KEY_PREFIX + productId);
        markDirty(productId);
    }

    public void decrementLike(Integer productId) {
        redisTemplate.opsForValue().decrement(LIKE_KEY_PREFIX + productId);
        markDirty(productId);
    }

    // TODO: 최종프로젝트에서 댓글 적용
    public void incrementComment(Integer productId) {
        redisTemplate.opsForValue().increment(COMMENT_KEY_PREFIX + productId);
        markDirty(productId);
    }

    public void incrementView(Integer productId) {
        redisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + productId);
        markDirty(productId);
    }

    private void markDirty(Integer productId) {
        redisTemplate.opsForSet().add(DIRTY_KEY, productId);
    }

    /** [Read] 변경된 ID 목록 조회 */
    public Set<Object> getDirtyProductIds() {
        return redisTemplate.opsForSet().members(DIRTY_KEY);
    }

    /** [Read] 여러 상품의 통계(조회/좋아요/댓글)를 한 번에 조회 (MGET) */
    public List<Object> getMultiStats(List<Integer> productIds) {
        List<String> keys = new ArrayList<>();
        for (Integer id : productIds) {
            keys.add(VIEW_KEY_PREFIX + id);
            keys.add(LIKE_KEY_PREFIX + id);
            keys.add(COMMENT_KEY_PREFIX + id);
        }

        return redisTemplate.opsForValue().multiGet(keys);
    }

    /** [Delete] 처리 완료된 Dirty Key 제거 */
    public void cleanupDirtyIds(Set<Object> processedIds) {
        redisTemplate.opsForSet().remove(DIRTY_KEY, processedIds.toArray());
    }

    // 유틸: Null Safe Parsing
    public long parseLongSafe(Object value) {
        if (value == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}