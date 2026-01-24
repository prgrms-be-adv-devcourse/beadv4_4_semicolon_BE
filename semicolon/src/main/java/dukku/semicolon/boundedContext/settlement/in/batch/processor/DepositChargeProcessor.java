package dukku.semicolon.boundedContext.settlement.in.batch.processor;

import dukku.semicolon.boundedContext.settlement.app.SettlementFacade;
import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.shared.settlement.exception.SettlementProcessingException;
import dukku.semicolon.shared.settlement.exception.SettlementValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * 예치금 충전 Processor
 * - Settlement를 PROCESSING 상태로 변경하고 예치금 충전 요청 이벤트 발행
 * - 예외 분류를 통해 Batch의 Skip/Retry 정책 적용
 *
 * Skip 대상 예외:
 * - SettlementValidationException: 데이터 유효성 오류 (재시도 불가)
 * - SettlementProcessingException: 비즈니스 처리 오류 (재시도 불가)
 *
 * Retry 대상 예외:
 * - DataAccessException: DB 연결 오류 (재시도 가능)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargeProcessor implements ItemProcessor<Settlement, Settlement> {

    private final SettlementFacade settlementFacade;

    @Override
    public Settlement process(Settlement settlement) throws Exception {
        log.debug("[Processor] 정산 처리 시작. settlementUuid={}, status={}",
                settlement.getUuid(), settlement.getSettlementStatus());

        try {
            // Settlement → PROCESSING 상태 변경 + 이벤트 발행
            Settlement processed = settlementFacade.requestDepositCharge(settlement);

            log.debug("[Processor] 정산 처리 성공. settlementUuid={}, newStatus={}",
                    processed.getUuid(), processed.getSettlementStatus());

            return processed;

        } catch (SettlementValidationException e) {
            // 데이터 유효성 오류 → Skip 처리
            // - 상태 전이 불가, 금액 오류, 필수 필드 누락 등
            // - 재시도해도 성공하지 않으므로 Skip
            log.error("[Processor-Skip] 데이터 유효성 오류로 Skip 처리. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;  // SkipListener에서 처리 (FAILED 상태로 변경)

        } catch (SettlementProcessingException e) {
            // 비즈니스 처리 오류 → Skip 처리
            // - 예치금 계좌 없음, 외부 서비스 호출 실패 등
            // - 재시도해도 성공하지 않으므로 Skip
            log.error("[Processor-Skip] 비즈니스 처리 오류로 Skip 처리. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;  // SkipListener에서 처리 (FAILED 상태로 변경)

        } catch (DataAccessException e) {
            // DB 접근 오류 → Retry 처리
            // - DB 커넥션 끊김, 데드락, 타임아웃 등
            // - 일시적 오류일 수 있으므로 Retry
            log.warn("[Processor-Retry] DB 접근 오류로 Retry 시도. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage());
            throw e;  // Batch의 RetryPolicy에 의해 재시도

        } catch (Exception e) {
            // 예상치 못한 오류 → Skip 처리
            // - 분류되지 않은 오류는 Skip으로 처리하여 Job 전체 실패 방지
            log.error("[Processor-Skip] 예상치 못한 오류로 Skip 처리. settlementUuid={}, error={}",
                    settlement.getUuid(), e.getMessage(), e);
            throw new SettlementProcessingException(
                    "정산 처리 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }
}
