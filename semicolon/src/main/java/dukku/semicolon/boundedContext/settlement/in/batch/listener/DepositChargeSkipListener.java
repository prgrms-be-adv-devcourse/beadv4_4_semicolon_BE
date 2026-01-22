package dukku.semicolon.boundedContext.settlement.in.batch.listener;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

/**
 * 예치금 충전 Step Skip 리스너
 * read / process / write 중 skip 발생 시 호출됨(SkipListener)
 * - Skip 발생 시 상세 로깅
 * - 실패한 Settlement를 FAILED 상태로 변경
 * - 읽기: Settlement, 처리 결과:Settlement
 * - 단계	역할
 * - Read Skip	=> 로그
 * - Process Skip => Settlement → FAILED 전이 + 저장
 * - Write Skip	=> 로그
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositChargeSkipListener implements SkipListener<Settlement, Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("[DEPOSIT-SKIP-READ] 읽기 중 에러 발생 - Skip 처리됨");
        log.error("  - Exception: {}", t.getClass().getSimpleName());
        log.error("  - Message: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Settlement item, Throwable t) {
        log.error("[DEPOSIT-SKIP-PROCESS] 예치금 충전 처리 중 에러 발생 - Skip 처리됨");
        log.error("  - Settlement UUID: {}", item != null ? item.getUuid() : "null");
        log.error("  - Seller UUID: {}", item != null ? item.getSellerUuid() : "null");
        log.error("  - Exception: {}", t.getClass().getSimpleName());
        log.error("  - Message: {}", t.getMessage());

        // 실패한 Settlement를 FAILED 상태로 변경
        if (item != null) {
            try {
                item.fail();
                settlementRepository.save(item);
                log.info("Settlement 상태를 FAILED로 변경함 - UUID: {}", item.getUuid());
            } catch (Exception e) {
                log.error("Settlement FAILED 상태 변경 실패 - UUID: {}, Error: {}",
                        item.getUuid(), e.getMessage());
            }
        }
    }

    @Override
    public void onSkipInWrite(Settlement item, Throwable t) {
        log.error("[DEPOSIT-SKIP-WRITE] 저장 중 에러 발생 - Skip 처리됨");
        log.error("  - Settlement UUID: {}", item != null ? item.getUuid() : "null");
        log.error("  - Exception: {}", t.getClass().getSimpleName());
        log.error("  - Message: {}", t.getMessage());
    }
}
