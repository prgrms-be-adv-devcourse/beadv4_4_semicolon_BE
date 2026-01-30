package dukku.semicolon.boundedContext.product.app.cqrs;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductBatchScheduler {
    private final ProductSyncFacade productSyncFacade;

    @Scheduled(fixedRate = 60000) // 1분
    public void scheduleSync() {
        productSyncFacade.syncAllStats();
    }
}