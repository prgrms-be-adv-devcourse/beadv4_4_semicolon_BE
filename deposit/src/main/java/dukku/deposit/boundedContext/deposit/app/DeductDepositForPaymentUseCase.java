package dukku.deposit.boundedContext.deposit.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.deposit.event.DepositDeductionFailedEvent;
import dukku.common.shared.deposit.event.DepositUsedEvent;
import dukku.common.shared.deposit.type.DepositFailureCode;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.common.shared.deposit.type.DepositHistoryType;
import dukku.deposit.boundedContext.deposit.exception.NotEnoughDepositException;
import dukku.deposit.global.SystemDepositInitData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 결제에 따른 예치금 차감 UseCase (Saga 패턴 참여)
 *
 * <p>
 * 결제 성공 시 각 상품별로 할당된 예치금만큼 차감을 진행하고,
 * 전체 차감 결과를 이벤트를 통해 전파한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeductDepositForPaymentUseCase {

    private final DecreaseDepositUseCase decreaseDepositUseCase;
    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final EventPublisher eventPublisher;

    /**
     * 결제에 따른 예치금 차감 실행
     *
     * @param userUuid          예치금을 소유한 유저 식별자
     * @param totalAmount       차감될 총 예치금액
     * @param orderUuid         관련 주문 식별자
     * @param paymentUuid       관련 결제 식별자(보상 트리거용)
     * @param itemDepositUsages 상품별 예치금 사용 상세 내역
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(UUID userUuid, Long totalAmount, UUID orderUuid, UUID paymentUuid,
            List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages) {
        if (totalAmount == null || totalAmount <= 0) {
            return;
        }

        try {
            executeDeductions(userUuid, totalAmount, orderUuid, itemDepositUsages);
        } catch (Exception e) {
            // 실패 시 부분 차감 커밋 방지: 현재 트랜잭션을 반드시 롤백시킨다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            handleDeductionError(userUuid, totalAmount, orderUuid, paymentUuid, e);
        }
    }

    private void executeDeductions(UUID userUuid, Long totalAmount, UUID orderUuid,
            List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages) {
        // 상품별 예치금 차감 및 이력 생성
        for (PaymentSuccessEvent.ItemDepositUsage usage : itemDepositUsages) {
            decreaseDepositUseCase.decrease(userUuid, usage.depositAmount(), DepositHistoryType.USE,
                    usage.orderItemUuid());
        }

        // 전체 차감 완료 성공 이벤트 발행
        // 시스템 지갑 예치금 증가
        increaseDepositUseCase.increase(
                SystemDepositInitData.SYSTEM_USER_UUID,
                totalAmount,
                DepositHistoryType.DEPOSIT_CHARGE,
                orderUuid);

        // 전체 차감 완료 성공 이벤트 발행
        eventPublisher.publish(new DepositUsedEvent(orderUuid, userUuid, totalAmount));
    }

    private void handleDeductionError(UUID userUuid, Long amount, UUID orderUuid, UUID paymentUuid, Exception e) {
        String errorMessage = "시스템 오류가 발생했습니다.";
        String logMessage = "[예치금 차감 실패 - 시스템 오류] userUuid={}, amount={}, orderUuid={}";
        DepositFailureCode failureCode = DepositFailureCode.SYSTEM_ERROR; // 예치금 차감 시스템 오류
        boolean retryable = true; // 시스템 오류는 재시도 가능

        if (e instanceof NotEnoughDepositException) {
            errorMessage = e.getMessage();
            logMessage = "[예치금 차감 실패 - 잔액 부족] userUuid={}, amount={}, orderUuid={}";
            failureCode = DepositFailureCode.BALANCE_SHORTAGE; // 잔액 부족으로 차감 실패
            retryable = false; // 잔액 부족은 재시도 의미 없음
            log.warn(logMessage, userUuid, amount, orderUuid);
        } else {
            log.error(logMessage, userUuid, amount, orderUuid, e);
        }

        // 결제 보상 트리거용 paymentUuid 전파
        eventPublisher.publish(new DepositDeductionFailedEvent(
                orderUuid,
                paymentUuid,
                userUuid,
                amount,
                failureCode,
                retryable,
                errorMessage,
                LocalDateTime.now()));
    }
}
