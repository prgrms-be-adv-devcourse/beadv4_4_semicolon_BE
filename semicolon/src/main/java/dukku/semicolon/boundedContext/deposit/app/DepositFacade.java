package dukku.semicolon.boundedContext.deposit.app;

import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import dukku.semicolon.shared.deposit.dto.DepositDto;
import dukku.semicolon.shared.deposit.dto.DepositHistoryDto;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.deposit.event.*;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.semicolon.boundedContext.deposit.exception.NotEnoughDepositException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositFacade {

    private final FindDepositUseCase findDepositUseCase;
    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final DecreaseDepositUseCase decreaseDepositUseCase;
    private final FindDepositHistoriesUseCase findDepositHistoriesUseCase;

    private final EventPublisher eventPublisher;

    /**
     * 사용자 예치금 조회
     *
     * <p>
     * 사용자의 예치금 정보를 조회하고, 존재하지 않을 경우 0원의 예치금 계좌를 생성하여 반환한다.
     */
    public DepositDto findDeposit(UUID userUuid) {
        return findDepositUseCase.findOrCreate(userUuid).toDto();
    }

    /**
     * 예치금 잔액 증가 (충전/정산/환불)
     *
     * <p>
     * 예치금 잔액을 증가시키고 해당 내역을 저장한다.
     */
    public void increaseDeposit(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        increaseDepositUseCase.increase(userUuid, amount, type, orderItemUuid);
    }

    /**
     * 예치금 잔액 차감 (사용/롤백)
     *
     * <p>
     * 예치금 잔액을 차감하고 해당 내역을 저장한다. 잔액 부족 시 예외 발생.
     */
    public void decreaseDeposit(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        decreaseDepositUseCase.decrease(userUuid, amount, type, orderItemUuid);
    }

    /**
     * 예치금 변동 내역 조회 (커서 기반 페이징)
     *
     * <p>
     * 사용자의 예치금 변동 내역을 최신순으로 조회한다.
     */
    public Slice<DepositHistoryDto> findHistories(UUID userUuid, Integer cursor, int size) {
        Slice<DepositHistory> histories = findDepositHistoriesUseCase.findHistories(userUuid, cursor, size);
        List<DepositHistoryDto> content = histories.getContent().stream()
                .map(DepositHistory::toDto)
                .toList();
        return new SliceImpl<>(content, histories.getPageable(), histories.hasNext());
    }

    /**
     * 전체 예치금 변동 내역 조회 (관리자용, 커서 기반 페이징)
     *
     * <p>
     * 시스템 상의 모든 예치금 변동 내역을 최신순으로 조회한다.
     */
    public Slice<DepositHistoryDto> findAllHistories(Integer cursor, int size) {
        Slice<DepositHistory> histories = findDepositHistoriesUseCase.findAllHistories(cursor, size);
        List<DepositHistoryDto> content = histories.getContent().stream()
                .map(DepositHistory::toDto)
                .toList();
        return new SliceImpl<>(content, histories.getPageable(), histories.hasNext());
    }

    /**
     * @deprecated Use {@link #findHistories(UUID, Integer, int)} instead.
     */
    @Deprecated
    public List<DepositHistoryDto> findHistories(UUID userUuid) {
        return findDepositHistoriesUseCase.findHistories(userUuid).stream()
                .map(DepositHistory::toDto)
                .toList();
    }

    /**
     * @deprecated Use {@link #findAllHistories(Integer, int)} instead.
     */
    @Deprecated
    public List<DepositHistoryDto> findAllHistories() {
        return findDepositHistoriesUseCase.findAllHistories().stream()
                .map(DepositHistory::toDto)
                .toList();
    }

    /**
     * 결제 및 주문 처리에 따른 예치금 차감 프로세스 (Saga 패턴 참여)
     *
     * <p>
     * 결제 성공 시 각 상품별로 할당된 예치금만큼 차감을 진행하고, 전체 차감 결과를 이벤트를 통해 전파한다.
     * 
     * @param userUuid          예치금을 소유한 유저 식별자
     * @param totalAmount       차감될 총 예치금액 (동일성 검증용)
     * @param orderUuid         관련 주문 식별자
     * @param itemDepositUsages 상품별 예치금 사용 상세 내역 (차감 로직의 핵심 데이터)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductDepositForPayment(UUID userUuid, Long totalAmount, UUID orderUuid,
            List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages) {
        if (totalAmount == null || totalAmount <= 0) {
            return;
        }

        try {
            executeDeductions(userUuid, totalAmount, orderUuid, itemDepositUsages);
        } catch (Exception e) {
            // 차감 중 예외 발생 시 (잔액 부족 등) 실패 처리를 위한 보상 트랜잭션 유도
            handleDeductionError(userUuid, totalAmount, orderUuid, e);
        }
    }

    /**
     * 실제 예치금 차감 및 성공 이벤트 발행
     */
    private void executeDeductions(UUID userUuid, Long totalAmount, UUID orderUuid,
            List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages) {

        // 상품별 예치금 차감 및 이력 생성
        // 전달받은 아이템별 사용액 정보를 바탕으로 순차적으로 차감 로직을 수행함
        for (PaymentSuccessEvent.ItemDepositUsage usage : itemDepositUsages) {
            decreaseDeposit(userUuid, usage.depositAmount(), DepositHistoryType.USE, usage.orderItemUuid());
        }

        // 전체 차감 완료 성공 이벤트 발행
        eventPublisher.publish(new DepositUsedEvent(
                orderUuid,
                userUuid,
                totalAmount));
    }

    private void handleDeductionError(UUID userUuid, Long amount, UUID orderUuid, Exception e) {
        String errorMessage = "시스템 오류가 발생했습니다.";
        String logMessage = "[예치금 차감 실패 - 시스템 오류] userUuid={}, amount={}, orderUuid={}";

        if (e instanceof NotEnoughDepositException) {
            errorMessage = e.getMessage();
            logMessage = "[예치금 차감 실패 - 잔액 부족] userUuid={}, amount={}, orderUuid={}";
            log.warn(logMessage, userUuid, amount, orderUuid);
        } else {
            log.error(logMessage, userUuid, amount, orderUuid, e);
        }

        eventPublisher.publish(new DepositDeductionFailedEvent(
                orderUuid,
                userUuid,
                amount,
                errorMessage));
    }

    /**
     * 환불 처리 (Saga 참여)
     *
     * <p>
     * 환불 발생 시 예치금을 롤백(재적립)하고 결과를 발행한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refundDeposit(UUID userUuid, Long amount, UUID orderUuid) {
        if (amount == null || amount <= 0) {
            return;
        }

        try {
            increaseDeposit(userUuid, amount, DepositHistoryType.ROLLBACK, orderUuid);

            eventPublisher.publish(new DepositRefundedEvent(
                    orderUuid,
                    userUuid,
                    amount));

        } catch (Exception e) {
            log.error("[예치금 환불/롤백 실패] userUuid={}, amount={}, orderUuid={}", userUuid, amount, orderUuid, e);
            // 환불중 예외 발생시 실패 로그
        }
    }

    /**
     * 정산에 의한 예치금 충전 (Saga 참여)
     *
     * <p>
     * 정산 완료 시 예치금을 충전하고 성공/실패 이벤트를 발행한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void chargeDepositForSettlement(UUID userUuid, Long amount, UUID settlementUuid) {
        if (amount == null || amount <= 0) {
            return;
        }

        try {
            executeCharge(userUuid, amount, settlementUuid);
        } catch (Exception e) {
            handleChargeError(userUuid, amount, settlementUuid, e);
        }
    }

    private void executeCharge(UUID userUuid, Long amount, UUID settlementUuid) {
        // 정산에 의한 충전은 SETTLEMENT 타입 사용
        increaseDeposit(userUuid, amount, DepositHistoryType.SETTLEMENT, settlementUuid);

        // 성공 이벤트 발행
        eventPublisher.publish(new DepositChargeSucceededEvent(
                userUuid,
                amount,
                settlementUuid));
    }

    private void handleChargeError(UUID userUuid, Long amount, UUID settlementUuid, Exception e) {
        log.error("[정산 예치금 충전 실패] userUuid={}, amount={}, settlementUuid={}", userUuid, amount, settlementUuid, e);

        // 실패 이벤트 발행
        eventPublisher.publish(new DepositChargeFailedEvent(
                userUuid,
                amount,
                settlementUuid,
                e.getMessage()));
    }
}
