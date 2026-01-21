package dukku.semicolon.boundedContext.settlement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 정산 수수료 정책
 * 수수료율 관련 정책을 한 곳에서 관리
 */
public final class SettlementFeePolicy {


    public static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.05");
    public static final BigDecimal DEFAULT_SETTLEMENT_RATE = new BigDecimal("0.95");

    private SettlementFeePolicy() {
        // 인스턴스 생성 방지
    }

    /**
     * 수수료율 결정
     * - 요청된 수수료율이 있으면 사용(나중에 이벤트별로 수수로 정책 가능?)
     * - 없으면 기본 수수료율 적용
     */
    public static BigDecimal resolve(BigDecimal requestedFeeRate) {
        return requestedFeeRate != null ? requestedFeeRate : DEFAULT_FEE_RATE;
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
        return new BigDecimal(totalAmount)
                .multiply(feeRate)
                .setScale(0, RoundingMode.FLOOR)
                .longValue();
    }
}
