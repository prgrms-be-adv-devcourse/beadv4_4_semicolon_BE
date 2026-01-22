package dukku.semicolon.boundedContext.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SettlementFeePolicy {

    private SettlementFeePolicy() {
        // 인스턴스 생성 방지
    }

    /**
     * 정산 금액 계산 (총액 - 수수료)
     */
    public static long calculateSettlementAmount(Long totalAmount, BigDecimal feeRate) {
        long feeAmount = calculateFeeAmount(totalAmount, feeRate);
        return totalAmount - feeAmount;
    }

    /**
     * 수수료 금액 계산 (총액 * 수수료율)
     * - 소수점 이하 버림 (FLOOR)
     */
    public static long calculateFeeAmount(Long totalAmount, BigDecimal feeRate) {
        if (feeRate == null) {
            throw new IllegalArgumentException("수수료율(feeRate)은 필수입니다. application.yml에서 설정하세요.");
        }
        return new BigDecimal(totalAmount)
                .multiply(feeRate)
                .setScale(0, RoundingMode.FLOOR)
                .longValue();
    }
}
