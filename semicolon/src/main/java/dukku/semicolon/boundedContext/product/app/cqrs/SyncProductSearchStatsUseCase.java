package dukku.semicolon.boundedContext.product.app.cqrs;

import dukku.semicolon.shared.product.dto.cqrs.ProductStatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncProductSearchStatsUseCase {
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "products_v1";

    public void execute(List<ProductStatDto> stats) {
        if (stats == null || stats.isEmpty()) {
            return;
        }

        log.info("Syncing stats to ES. Count: {}", stats.size());

        List<UpdateQuery> updates = new ArrayList<>();

        for (ProductStatDto stat : stats) {
            String docId = String.valueOf(stat.productId());

            Map<String, Object> updatesMap = new HashMap<>();
            updatesMap.put("viewCount", stat.viewCount());
            updatesMap.put("likeCount", stat.likeCount());
            updatesMap.put("commentCount", stat.commentCount());

            updates.add(UpdateQuery.builder(docId)
                    .withDocument(Document.from(updatesMap))
                    .build());
        }

        // ES 저장 실행
        elasticsearchOperations.bulkUpdate(updates, IndexCoordinates.of(INDEX_NAME));
    }
}