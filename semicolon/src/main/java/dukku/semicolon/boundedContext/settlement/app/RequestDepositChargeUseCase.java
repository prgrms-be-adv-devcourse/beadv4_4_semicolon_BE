package dukku.semicolon.boundedContext.settlement.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.settlement.event.SettlementDepositChargeRequestedEvent;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예치금 충전 요청 UseCase
 * - Settlement의 정산금액을 판매자 예치금에 충전 요청
 * - Settlement 상태: PENDING → PROCESSING
 * - Deposit BC에서 충전 완료/실패 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestDepositChargeUseCase {

    private final SettlementSupport settlementSupport;
    private final EventPublisher eventPublisher;

    @Transactional
    public Settlement execute(Settlement settlement) {
        log.info("[예치금 충전 요청] settlementUuid={}, sellerUuid={}, amount={}",
                settlement.getUuid(), settlement.getSellerUuid(), settlement.getSettlementAmount());

        try {
            // 1. Settlement 상태 전이 검증 (내부에서 validateForProcessing 호출)
            // - IllegalStateException: 상태 전이 불가 → SettlementValidationException 변환
            // - SettlementValidationException: 데이터 유효성 오류
            settlement.startProcessing();
            settlementSupport.save(settlement);

            // 2. 예치금 충전 요청 이벤트 발행 (Deposit BC로)
            eventPublisher.publish(
                    new SettlementDepositChargeRequestedEvent(
                            settlement.getSellerUuid(),       // 판매자 UUID
                            settlement.getSettlementAmount(), // 정산 금액 (수수료 제외)
                            settlement.getUuid()              // 정산 UUID
                    )
            );

            log.info("[예치금 충전 요청 이벤트 발행] settlementUuid={}, amount={}",
                    settlement.getUuid(), settlement.getSettlementAmount());

            return settlement;

        } catch (IllegalStateException e) {
            // 상태 전이 불가 (PENDING 아닌 상태에서 PROCESSING 시도)
            log.error("[정산 처리 실패] 상태 전이 불가. settlementUuid={}, currentStatus={}, error={}",
                    settlement.getUuid(), settlement.getSettlementStatus(), e.getMessage());
            throw SettlementValidationException.invalidStatusTransition(
                    settlement.getSettlementStatus().name(), "PROCESSING");

        } catch (SettlementValidationException e) {
            // 데이터 유효성 검증 실패 (금액 오류, 필수 필드 누락 등)
            log.error("[정산 처리 실패] 데이터 유효성 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;

        } catch (Exception e) {
            // 예상치 못한 오류 (DB 오류, 이벤트 발행 실패 등)
            log.error("[정산 처리 실패] 예상치 못한 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage(), e);
            throw SettlementProcessingException.externalServiceFailed(
                    "EventPublisher", e.getMessage());
        }
    }
}
