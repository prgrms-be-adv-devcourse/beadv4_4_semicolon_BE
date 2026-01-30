package dukku.semicolon.boundedContext.product.app.cqrs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductSyncFacade {
    private final ProductStatsUseCase productStatsUseCase;

    @Transactional
    public void syncAllStats() {
        productStatsUseCase.execute();
    }
}