package dukku.semicolon.boundedContext.product.in;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.order.event.OrderProductSaleConfirmedEvent;
import dukku.common.shared.order.event.OrderProductSaleReleasedEvent;
import dukku.semicolon.boundedContext.product.app.cqrs.SaveToElasticSearchUseCase;
import dukku.semicolon.boundedContext.product.app.cqrs.SyncProductSearchStatsUseCase;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.boundedContext.product.out.ProductSearchRepository;
import dukku.semicolon.shared.product.event.ProductCreatedEvent;
import dukku.semicolon.shared.product.event.ProductDeletedEvent;
import dukku.semicolon.shared.product.event.ProductStatsBulkUpdatedEvent;
import dukku.semicolon.shared.product.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventListener {
    private final ProductSearchRepository productSearchRepository;
    private final SaveToElasticSearchUseCase saveToElasticSearchUseCase;
    private final SyncProductSearchStatsUseCase  syncProductSearchStatsUseCase;
    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    // 1. 생성 동기화
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void syncCreate(ProductCreatedEvent event) {
        saveToElasticSearchUseCase.execute(event.product());
    }

    // 2. 수정 동기화
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void syncUpdate(ProductUpdatedEvent event) {
        log.info("Sync Update Product: {}", event.product().getId());

        saveToElasticSearchUseCase.execute(event.product());
    }

    // 3. 삭제 동기화
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void syncDelete(ProductDeletedEvent event) {
        String docId = String.valueOf(event.product().getId());
        log.info("Sync Delete Product: {}", docId);

        productSearchRepository.deleteById(docId);
    }

    // 4. 통계 동기화 (배치 작업 후 실행)
    @Async
    @EventListener
    public void syncStats(ProductStatsBulkUpdatedEvent event) {
        syncProductSearchStatsUseCase.execute(event.getStats());
    }

    /**
     * 1. 결제 완료 -> 상품 품절 처리 (SOLD_OUT)
     * TransactionPhase.AFTER_COMMIT: 주문 트랜잭션이 완전히 끝난 후 실행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderConfirmed(OrderProductSaleConfirmedEvent event) {
        log.info("Handle Order Confirmed: orderUuid={}", event.orderUuid());

        List<Product> products = productRepository.findAllByUuidIn(event.productUuids());

        for (Product product : products) {
            // 1. DB 상태 변경
            product.confirmSale(event.orderUuid());

            // 2. 변경된 상태를 ES에 동기화하기 위해 ProductUpdatedEvent 발행
            // (이전에 만든 ProductEventListener가 이걸 잡아서 ES 업데이트 수행)
            eventPublisher.publish(new ProductUpdatedEvent(product));
        }
    }

    /**
     * 2. 결제 실패/취소 -> 상품 판매중 복구 (ON_SALE)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderReleased(OrderProductSaleReleasedEvent event) {
        log.info("Handle Order Released: orderUuid={}", event.orderUuid());

        List<Product> products = productRepository.findAllByUuidIn(event.productUuids());

        for (Product product : products) {
            // 1. DB 상태 변경
            product.releaseReservation(event.orderUuid());

            // 2. ES 동기화 이벤트 발행
            eventPublisher.publish(new ProductUpdatedEvent(product));
        }
    }
}