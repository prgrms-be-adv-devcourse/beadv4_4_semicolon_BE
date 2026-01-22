package dukku.semicolon.boundedContext.settlement.in.batch.scheduler;

import dukku.semicolon.boundedContext.settlement.in.batch.config.SettlementBatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 정산 배치 스케줄러
 * - 매일 자정에 정산 배치 Job 실행
 * - batch.settlement.scheduler.enabled=true 인 경우에만 활성화
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.settlement.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SettlementJobScheduler {

    private final JobOperator jobOperator;
    private final Job settlementJob;
    private final SettlementBatchProperties batchProperties;

    /**
     * 매일 자정에 정산 배치 실행
     * cron 표현식은 application.yml에서 설정 가능
     * 기본값: "0 0 0 * * *" (매일 자정)
     */
    @Scheduled(cron = "${batch.settlement.scheduler.cron:0 0 0 * * *}")
    public void runSettlementJob() {
        log.info("========== 정산 배치 스케줄러 시작 ==========");
        
        try {
            JobParameters jobParameters = createJobParameters();
            
            log.info("정산 배치 Job 실행 - Parameters: {}", jobParameters);

            jobOperator.start(settlementJob, jobParameters);
            
            log.info("정산 배치 Job 실행 완료");
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("정산 배치가 이미 실행 중입니다.", e);
        } catch (JobRestartException e) {
            log.error("정산 배치 재시작 실패", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.error("정산 배치가 이미 완료된 인스턴스입니다.", e);
        } catch (InvalidJobParametersException e) {
            log.error("정산 배치 파라미터가 잘못되었습니다.", e);
        } catch (Exception e) {
            log.error("정산 배치 실행 중 예기치 않은 에러 발생", e);
        }
    }

    /**
     * 수동 실행용 메서드 (관리자 API에서 호출 가능)
     */
    public void runManually() {
        log.info("========== 정산 배치 수동 실행 ==========");
        runSettlementJob();
    }

    /**
     * Job 파라미터 생성
     * - timestamp: 실행 시점 (중복 실행 방지)
     * - executionDate: 실행 날짜 (로깅용)
     */
    private JobParameters createJobParameters() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String executionDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return new JobParametersBuilder()
                .addString("timestamp", timestamp)
                .addString("executionDate", executionDate)
                .toJobParameters();
    }
}
