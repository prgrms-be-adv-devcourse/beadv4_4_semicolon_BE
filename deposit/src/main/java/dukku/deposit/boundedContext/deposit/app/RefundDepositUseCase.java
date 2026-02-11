package dukku.deposit.boundedContext.deposit.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.deposit.event.DepositRefundedEvent;
import dukku.common.shared.deposit.event.DepositRefundFailedEvent;
import dukku.common.shared.deposit.type.DepositFailureCode;
import dukku.common.shared.deposit.type.DepositHistoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 환불 처리 UseCase (Saga 참여)
 *
 * <p>
 * 환불 발생 시 예치금을 롤백(재적립)하고 결과를 발행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundDepositUseCase {

    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final EventPublisher eventPublisher;

    /**
     * 환불 처리 실행
     *
     * @param userUuid  사용자 UUID
     * @param amount    환불 금액
     * @param orderUuid 주문 UUID
     * @param paymentUuid 결제 UUID (실패 이벤트 연계용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(UUID userUuid, Long amount, UUID orderUuid, UUID paymentUuid) {
        if (amount == null || amount <= 0) {
            return;
        }

        try {
            // 예치금 롤백 처리
            increaseDepositUseCase.increase(userUuid, amount, DepositHistoryType.ROLLBACK, orderUuid);

            // 롤백 성공 이벤트 발행
            eventPublisher.publish(new DepositRefundedEvent(orderUuid, userUuid, amount));

        } catch (Exception e) {
            log.error("[예치금 환불/롤백 실패] userUuid={}, amount={}, orderUuid={}", userUuid, amount, orderUuid, e);
            // 환불 실패 이벤트 발행 (보상/운영 추적)
            eventPublisher.publish(new DepositRefundFailedEvent(
                    orderUuid,
                    paymentUuid,
                    userUuid,
                    amount,
                    DepositFailureCode.PERSISTENCE_ERROR,
                    true,
                    buildFailureReason(DepositFailureCode.PERSISTENCE_ERROR, e.getMessage()),
                    LocalDateTime.now()));
        }
    }

    private String buildFailureReason(DepositFailureCode code, String detail) {
        if (detail == null || detail.isBlank()) {
            return code.name();
        }
        return code.name() + ": " + detail;
    }
}
