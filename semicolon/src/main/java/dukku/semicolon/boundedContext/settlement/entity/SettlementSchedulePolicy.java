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
     * - 월 1회 정산: 다음 달 1일 오전 2시
     * - 예: 2024-01-15 확정 → 2024-02-01 02:00 정산 예약
     * @param baseTime 기준 시간 (보통 현재 시간)
     * @return 정산 예약일
     */
    public static LocalDateTime calculateReservationDate(LocalDateTime baseTime) {
        return baseTime.plusMonths(1)
                .withDayOfMonth(1)
                .with(LocalTime.of(2, 0)); // 오전 2시
    }

}
