package dukku.semicolon.boundedContext.settlement.batch.processor;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 재처리 Processor
 * - FAILED 상태의 Settlement를 PENDING으로 복구
 * - 이후 정상 플로우(PENDING → PROCESSING → SUCCESS) 진행
 *
 * [책임]
 * - retry() 호출하여 FAILED → PENDING 상태 전이
 * - 금액 검증 및 예치금 충전은 이후 Step에서 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetrySettlementProcessor implements ItemProcessor<Settlement, Settlement> {

    @Override
    public Settlement process(Settlement settlement) throws Exception {
        log.debug("[Retry Processor] 재처리 시작. settlementUuid={}, status={}",
                settlement.getUuid(), settlement.getSettlementStatus());

        try {
            // FAILED → PENDING 상태 전이
            settlement.retry();

            log.info("[Retry Processor] 재처리 성공. settlementUuid={}, newStatus={}",
                    settlement.getUuid(), settlement.getSettlementStatus());

            return settlement;

        } catch (IllegalStateException e) {
            // 상태 전이 불가 → Skip 처리
            log.error("[Retry Processor-Skip] 상태 전이 불가. settlementUuid={}, currentStatus={}, error={}",
                    settlement.getUuid(), settlement.getSettlementStatus(), e.getMessage());
            throw new SettlementValidationException("재처리 상태 전이 불가: " + e.getMessage());

        } catch (Exception e) {
            // 예상치 못한 오류 → Skip 처리
            log.error("[Retry Processor-Skip] 예상치 못한 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage(), e);
            throw new SettlementValidationException("재처리 중 오류 발생: " + e.getMessage());
        }
    }
}
