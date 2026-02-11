package dukku.deposit.boundedContext.deposit.app;

import dukku.deposit.boundedContext.deposit.entity.DepositHistory;
import dukku.common.shared.deposit.type.DepositHistoryType;
import dukku.common.shared.deposit.dto.DepositDto;
import dukku.common.shared.deposit.dto.DepositHistoryDto;
import dukku.common.shared.deposit.dto.DepositChargeForSettlementResponse;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Deposit 도메인 Facade
 *
 * <p>
 * Controller가 호출하는 진입점. 각 UseCase로 위임만 수행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositFacade {

    private final FindDepositUseCase findDepositUseCase;
    private final FindDepositByDepositUuidUseCase findDepositByDepositUuidUseCase;
    private final IncreaseDepositUseCase increaseDepositUseCase;
    private final DecreaseDepositUseCase decreaseDepositUseCase;
    private final FindDepositHistoriesUseCase findDepositHistoriesUseCase;
    private final DeductDepositForPaymentUseCase deductDepositForPaymentUseCase;
    private final IncreaseSystemDepositForPgUseCase increaseSystemDepositForPgUseCase;
    private final RefundDepositUseCase refundDepositUseCase;
    private final ChargeDepositUseCase chargeDepositUseCase;
    private final ChargeDepositForSettlementUseCase chargeDepositForSettlementUseCase;

    /**
     * 사용자 예치금 조회
     */
    public DepositDto findDeposit(UUID userUuid) {
        return findDepositUseCase.findOrCreate(userUuid).toDto();
    }

    public DepositDto findDepositByDepositUuid(UUID depositUuid) {
        return findDepositByDepositUuidUseCase.execute(depositUuid).toDto();
    }

    /**
     * 예치금 잔액 증가 (충전/정산/환불)
     */
    public void increaseDeposit(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        increaseDepositUseCase.increase(userUuid, amount, type, orderItemUuid);
    }

    /**
     * 예치금 잔액 차감 (사용/롤백)
     */
    public void decreaseDeposit(UUID userUuid, Long amount, DepositHistoryType type, UUID orderItemUuid) {
        decreaseDepositUseCase.decrease(userUuid, amount, type, orderItemUuid);
    }

    /**
     * 예치금 변동 내역 조회 (커서 기반 페이징)
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
     * 결제에 따른 예치금 차감 (Saga 참여)
     */
    public void deductDepositForPayment(UUID userUuid, Long totalAmount, UUID orderUuid, UUID paymentUuid,
                                        List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages) {
        // paymentUuid 전달 (보상 트랜잭션 연계)
        deductDepositForPaymentUseCase.execute(userUuid, totalAmount, orderUuid, paymentUuid, itemDepositUsages);
    }

    /**
     * PG 결제 승인분을 시스템 지갑에 반영
     */
    public void increaseSystemDepositForPg(UUID orderUuid, Long pgAmount) {
        increaseSystemDepositForPgUseCase.execute(orderUuid, pgAmount);
    }

    /**
     * 환불 처리 (Saga 참여)
     */
    public void refundDeposit(UUID userUuid, Long amount, UUID orderUuid, UUID paymentUuid) {
        // paymentUuid 전달 (환불 실패 이벤트 연계)
        refundDepositUseCase.execute(userUuid, amount, orderUuid, paymentUuid);
    }

    /**
     * 정산에 의한 예치금 충전 (Saga 참여)
     *
     * @deprecated {@link #chargeDepositForSettlementApi(UUID, Long, UUID)} 사용을
     * 권장합니다.
     */
    @Deprecated
    public void chargeDepositForSettlement(UUID userUuid, Long amount, UUID settlementUuid) {
        log.warn(
                "[DEPRECATED] 이벤트 기반 정산 충전 로직(chargeDepositForSettlement)이 호출되었습니다. API 방식(chargeDepositForSettlementApi)으로의 전환이 필요합니다. settlementUuid={}",
                settlementUuid);
        chargeDepositUseCase.execute(userUuid, amount, settlementUuid);
    }

    /**
     * Internal API용 정산 예치금 충전
     */
    public DepositChargeForSettlementResponse chargeDepositForSettlementApi(
            UUID userUuid, Long amount, UUID settlementUuid) {
        return chargeDepositForSettlementUseCase.execute(userUuid, amount, settlementUuid);
    }

    /**
     * 시스템 초기 자본금 주입 (ADJUST 타입 사용)
     */
    public void injectSystemCapital(UUID userUuid, Long amount) {
        // orderItemUuid는 시스템 자본금 주입이므로 null 처리 (상품이 존재하지 않음)
        increaseDepositUseCase.increase(userUuid, amount, DepositHistoryType.ADJUST, null);
    }
}
