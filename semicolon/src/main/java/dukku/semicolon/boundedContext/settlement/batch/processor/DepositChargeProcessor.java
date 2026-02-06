package dukku.semicolon.boundedContext.settlement.batch.processor;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.deposit.dto.DepositChargeForSettlementResponse;
import dukku.semicolon.shared.deposit.out.depositApiClient.DepositApiClient;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * Step 2: 예치금 충전 Processor
 * - Deposit BC API Client를 통한 예치금 충전 (동기 방식)
 * - 성공 시 Settlement 상태를 SUCCESS로 변경
 *
 * [Idempotency]
 * - 이미 SUCCESS/FAILED 상태인 Settlement는 Skip
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargeProcessor implements ItemProcessor<Settlement, Settlement> {

    private final DepositApiClient depositApiClient;

    @Override
    public Settlement process(Settlement settlement) throws Exception {
        log.debug("[Step 2 Processor] 예치금 충전 시작. settlementUuid={}, status={}",
                settlement.getUuid(), settlement.getSettlementStatus());

        // Idempotency: 이미 완료된 건은 Skip
        if (settlement.isCompleted()) {
            log.warn("[Step 2 Processor-Skip] 이미 정산 완료된 건. settlementUuid={}", settlement.getUuid());
            return null; // Writer로 전달하지 않음
        }

        if (settlement.isFailed()) {
            log.warn("[Step 2 Processor-Skip] 이미 실패 처리된 건. settlementUuid={}", settlement.getUuid());
            return null;
        }

        try {
            // Deposit BC API Client 호출하여 예치금 충전
            DepositChargeForSettlementResponse response = depositApiClient.chargeDepositForSettlement(
                    settlement.getSellerUuid(),
                    settlement.getSettlementAmount(),
                    settlement.getUuid()
            );

            if (response == null || !response.isSuccess()) {
                String errorMsg = response != null ? response.getMessage() : "응답 없음";
                log.error("[Step 2 Processor] 예치금 충전 실패. settlementUuid={}, error={}",
                        settlement.getUuid(), errorMsg);
                settlement.fail();
                throw new SettlementProcessingException("예치금 충전 실패: " + errorMsg);
            }

            // 성공 시 SUCCESS 상태로 변경
            settlement.complete();

            log.info("[Step 2 Processor] 예치금 충전 성공. settlementUuid={}, chargedAmount={}, balanceAfter={}",
                    settlement.getUuid(),
                    response.getData().getChargedAmount(),
                    response.getData().getBalanceAfter());

            return settlement;

        } catch (SettlementValidationException e) {
            // 데이터 유효성 오류 → Skip 처리
            log.error("[Step 2 Processor-Skip] 데이터 유효성 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;

        } catch (SettlementProcessingException e) {
            // 비즈니스 처리 오류 → Skip 처리
            log.error("[Step 2 Processor-Skip] 비즈니스 처리 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;

        } catch (DataAccessException e) {
            // DB 접근 오류 → Retry 처리
            log.warn("[Step 2 Processor-Retry] DB 접근 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;

        } catch (Exception e) {
            // 예상치 못한 오류 → Skip 처리
            log.error("[Step 2 Processor-Skip] 예상치 못한 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage(), e);
            throw new SettlementProcessingException(
                    "예치금 충전 중 오류 발생: " + e.getMessage());
        }
    }
}
