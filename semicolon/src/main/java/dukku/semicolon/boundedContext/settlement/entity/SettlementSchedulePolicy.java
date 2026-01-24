package dukku.semicolon.boundedContext.settlement.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;


public final class SettlementSchedulePolicy {

    private SettlementSchedulePolicy() {
        // 인스턴스 생성 방지
    }

    /**
     * 현재 시간 기준 다음 정산 예약일
     */
    public static LocalDateTime nextReservationDate() {
        return calculateReservationDate(LocalDateTime.now());
    }
    
    /**
     * 다음 정산 예약일 계산
     * - 구매확정 후 당일 자정 (다음 날 00:00)
     * @param baseTime 기준 시간 (보통 현재 시간)
     * @return 정산 예약일
     */
    public static LocalDateTime calculateReservationDate(LocalDateTime baseTime) {
        return baseTime.with(LocalTime.MIDNIGHT).plusDays(1);
    }

}
