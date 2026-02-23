package dukku.product.boundedContext.product.in.listener;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.user.event.UserAiInitializationFailedEvent;
import dukku.common.shared.user.event.UserDepositInitializedEvent;
import dukku.common.shared.user.event.UserProductInitializationFailedEvent;
import dukku.common.shared.user.event.UserProductInitializedEvent;
import dukku.product.boundedContext.product.entity.ProductUser;
import dukku.product.boundedContext.product.out.ProductUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUserEventListener {

    private final ProductUserRepository productUserRepository;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "user.deposit-initialized", groupId = "${spring.application.name}-group")
    public void handleDepositInitialized(UserDepositInitializedEvent event) {
        try {
            UUID userUuid = event.userUuid();

            if (!productUserRepository.existsById(userUuid)) {
                productUserRepository.save(ProductUser.create(userUuid, event.nickname()));
            }

            eventPublisher.publish(new UserProductInitializedEvent(userUuid, event.nickname()));
            log.info("[UserDepositInitializedEvent] Product user initialization completed. userUuid={}", userUuid);
        } catch (Exception e) {
            publishProductInitFailed(event, e);
        }
    }

    @KafkaListener(topics = "user.ai-initialization-failed", groupId = "${spring.application.name}-group")
    public void handleAiInitializationFailed(UserAiInitializationFailedEvent event) {
        try {
            UUID userUuid = event.userUuid();
            productUserRepository.findById(userUuid).ifPresent(productUserRepository::delete);
            eventPublisher.publish(new UserProductInitializationFailedEvent(userUuid, event.reason()));
            log.warn("[UserAiInitializationFailedEvent] Product user compensation completed. userUuid={}", userUuid);
        } catch (Exception e) {
            log.error("[UserAiInitializationFailedEvent] Product user compensation failed. userUuid={}", event.userUuid(), e);
        }
    }

    private void publishProductInitFailed(UserDepositInitializedEvent event, Exception cause) {
        String reason = cause.getMessage() == null ? "Product user initialization exception" : cause.getMessage();
        eventPublisher.publish(new UserProductInitializationFailedEvent(event.userUuid(), reason));
        log.error("[UserDepositInitializedEvent] Product user initialization failed. userUuid={}", event.userUuid(), cause);
    }
}
