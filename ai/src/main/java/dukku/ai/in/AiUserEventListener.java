package dukku.ai.in;

import dukku.ai.app.usecase.InitializeAiUserUseCase;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.user.event.UserAiInitializationFailedEvent;
import dukku.common.shared.user.event.UserProductInitializedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiUserEventListener {

    private final InitializeAiUserUseCase initializeAiUserUseCase;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "user.product-initialized", groupId = "${spring.application.name}-group")
    public void handleProductInitialized(UserProductInitializedEvent event) {
        try {
            initializeAiUserUseCase.execute(event.userUuid(), event.nickname());
            log.info("[UserProductInitializedEvent] AI user initialization completed. userUuid={}", event.userUuid());
        } catch (Exception e) {
            publishAiInitFailed(event, e);
        }
    }

    private void publishAiInitFailed(UserProductInitializedEvent event, Exception cause) {
        String reason = cause.getMessage() == null ? "AI user initialization exception" : cause.getMessage();
        eventPublisher.publish(new UserAiInitializationFailedEvent(event.userUuid(), reason));
        log.error("[UserProductInitializedEvent] AI user initialization failed. userUuid={}", event.userUuid(), cause);
    }
}
