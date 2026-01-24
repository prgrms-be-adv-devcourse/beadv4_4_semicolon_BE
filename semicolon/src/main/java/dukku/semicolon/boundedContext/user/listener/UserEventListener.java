package dukku.semicolon.boundedContext.user.listener;

import dukku.semicolon.shared.user.event.UserJoinedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class UserEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserJoined(UserJoinedEvent event) {
        log.info("[UserJoinedEvent] userUuid={}", event.member().userUuid());

        // 여기서 후처리 로직
        // 예)
        // - 웰컴 로그
        // - 기본 설정 생성
        // - 포인트 지급 (나중에 다른 BC 리스너로 분리 가능)
    }
}
