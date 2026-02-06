package dukku.semicolon.boundedContext.settlement.batch.processor;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Step 2: 금액 검증 Processor
 * - Settlement 금액 유효성 검증
 * - PENDING → PROCESSING 상태 전이
 *
 * [책임]
 * - 금액 검증만 수행 (Side Effect 최소화)
 * - 실제 예치금 충전은 Step 3에서 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateSettlementProcessor implements ItemProcessor<Settlement, Settlement> {

    @Override
    public Settlement process(Settlement settlement) throws Exception {
        log.debug("[Step 2 Processor] 금액 검증 시작. settlementUuid={}, status={}",
                settlement.getUuid(), settlement.getSettlementStatus());

        try {
            // 1. PENDING → PROCESSING 상태 전이 (내부에서 금액 검증 수행)
            // Settlement.startProcessing() 내부에서 validateForProcessing() 호출
            settlement.startProcessing();

            log.debug("[Step 2 Processor] 금액 검증 성공. settlementUuid={}, newStatus={}",
                    settlement.getUuid(), settlement.getSettlementStatus());

            return settlement;

        } catch (SettlementValidationException e) {
            // 데이터 유효성 오류 → Skip 처리
            log.error("[Step 2 Processor-Skip] 금액 검증 실패. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;

        } catch (IllegalStateException e) {
            // 상태 전이 불가 → Skip 처리
            log.error("[Step 2 Processor-Skip] 상태 전이 불가. settlementUuid={}, currentStatus={}, error={}",
                    settlement.getUuid(), settlement.getSettlementStatus(), e.getMessage());
            throw new SettlementValidationException("상태 전이 불가: " + e.getMessage());

        } catch (Exception e) {
            // 예상치 못한 오류 → Skip 처리
            log.error("[Step 2 Processor-Skip] 예상치 못한 오류. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage(), e);
            throw new SettlementValidationException("금액 검증 중 오류 발생: " + e.getMessage());
        }
    }
}
