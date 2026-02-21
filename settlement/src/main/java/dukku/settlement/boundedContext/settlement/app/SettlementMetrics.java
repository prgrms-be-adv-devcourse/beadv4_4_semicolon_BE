package dukku.settlement.boundedContext.settlement.app;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class SettlementMetrics {

    private final MeterRegistry meterRegistry;

    private Counter createdCounter;
    private Counter amountCounter;
    private Counter retryCounter;
    private Counter skipCounter;

    // Gauge용 — 마지막 배치 실행 시간 (초)
    private final AtomicLong lastJobDurationSeconds = new AtomicLong(0);

    @PostConstruct
    void init() {
        this.createdCounter = Counter.builder("business_settlement_created_total")
                .description("정산 생성 건수")
                .register(meterRegistry);
        this.amountCounter = Counter.builder("business_settlement_amount_total")
                .description("정산 금액 누적 합계")
                .register(meterRegistry);
        this.retryCounter = Counter.builder("business_settlement_retry_total")
                .description("정산 재처리 요청 건수")
                .register(meterRegistry);
        this.skipCounter = Counter.builder("business_settlement_skip_total")
                .description("정산 배치 Skip 건수")
                .register(meterRegistry);

        // Gauge: 마지막 배치 실행 시간
        Gauge.builder("business_settlement_last_duration_seconds", lastJobDurationSeconds, AtomicLong::doubleValue)
                .description("마지막 정산 배치 처리 소요 시간 (초)")
                .register(meterRegistry);
    }

    public void incrementCreated() {
        createdCounter.increment();
    }

    public void addAmount(double amount) {
        amountCounter.increment(amount);
    }

    public void incrementRetry() {
        retryCounter.increment();
    }

    public void incrementSkip() {
        skipCounter.increment();
    }

    public void incrementSkip(long count) {
        skipCounter.increment(count);
    }

    /**
     * 마지막 배치 실행 시간 기록 (초 단위)
     */
    public void recordJobDuration(long durationSeconds) {
        lastJobDurationSeconds.set(durationSeconds);
    }

    public void incrementAnomalyDetected(String anomalyType, String severity, String settlementUuid) {
        Counter.builder("settlement_anomaly_detected_total")
                .tag("type", anomalyType)
                .tag("severity", severity)
                .tag("settlement_uuid", settlementUuid)
                .description("이상거래 탐지 건수")
                .register(meterRegistry)
                .increment();
    }
}
