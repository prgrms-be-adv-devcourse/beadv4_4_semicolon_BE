package dukku.semicolon.boundedContext.product.app.cqrs;

import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.cqrs.ProductStatDto;
import dukku.semicolon.shared.product.event.ProductStatsBulkUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductStatsUseCase {
    private final ProductRepository productRepository;
    private final ProductStatsRedisSupport redisSupport;
    private final ApplicationEventPublisher eventPublisher;

    public void execute() {
        // 1. 조회
        Set<Object> dirtyIdSet = redisSupport.getDirtyProductIds();
        if (dirtyIdSet == null || dirtyIdSet.isEmpty()) return;

        List<Integer> productIds = dirtyIdSet.stream()
                .map(id -> Integer.parseInt(String.valueOf(id)))
                .toList();

        // 2. Redis 데이터 가져오기
        List<Object> statsValues = redisSupport.getMultiStats(productIds);
        if (statsValues == null || statsValues.isEmpty()) return;

        // 3. 업데이트 처리
        processUpdates(productIds, statsValues);

        // 4. 정리
        redisSupport.cleanupDirtyIds(dirtyIdSet);

        log.info("Synced stats for {} products", productIds.size());
    }

    private void processUpdates(List<Integer> productIds, List<Object> statsValues) {
        List<ProductStatDto> updateBatchList = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            int productId = productIds.get(i);
            int baseIdx = i * 3;

            long viewCount = redisSupport.parseLongSafe(statsValues.get(baseIdx));
            long likeCount = redisSupport.parseLongSafe(statsValues.get(baseIdx + 1));
            long commentCount = redisSupport.parseLongSafe(statsValues.get(baseIdx + 2));

            // 리스트에 담기
            updateBatchList.add(new ProductStatDto(productId, viewCount, likeCount, commentCount));
        }

        if (!updateBatchList.isEmpty()) {
            // 1. DB Bulk Update (JDBC)
            productRepository.bulkUpdateProductStats(updateBatchList);

            // 2. [변경점] ES Bulk Update를 위한 "통합 이벤트" 1회 발행
            eventPublisher.publishEvent(new ProductStatsBulkUpdatedEvent(updateBatchList));
        }
    }
}