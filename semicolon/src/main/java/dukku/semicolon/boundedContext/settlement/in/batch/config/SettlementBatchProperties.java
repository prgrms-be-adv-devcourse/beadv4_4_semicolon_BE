package dukku.semicolon.boundedContext.settlement.in.batch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "batch.settlement")
public class SettlementBatchProperties {


    private int chunkSize = 100;

    private BigDecimal feeRate = new BigDecimal("0.05");

    private int skipLimit = 10;

    private int retryLimit = 3;

    private int pageSize = 100;

    private Scheduler scheduler = new Scheduler();

    @Getter
    @Setter
    public static class Scheduler {
        private boolean enabled = true;

        private String cron = "0 0 0 * * *";
    }
}
