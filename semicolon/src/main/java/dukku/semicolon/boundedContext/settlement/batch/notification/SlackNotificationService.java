package dukku.semicolon.boundedContext.settlement.batch.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slack 알림 서비스
 * - 정산 배치 완료 시 Slack으로 알림 전송
 */
@Slf4j
@Service
public class SlackNotificationService {

    @Value("${slack.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public SlackNotificationService(@Qualifier("slackRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 정산 배치 완료 알림 전송
     */
    public void sendJobCompletionNotification(JobExecution jobExecution) {
        String message = buildMessage(jobExecution);
        sendSlackMessage(message);
    }

    private String buildMessage(JobExecution jobExecution) {
        StringBuilder sb = new StringBuilder();

        String status = jobExecution.getStatus().toString();
        String statusEmoji = "COMPLETED".equals(status) ? ":white_check_mark:" : ":x:";

        // 헤더
        sb.append(statusEmoji).append(" *정산 배치 완료 알림*\n\n");

        // Job 정보
        sb.append("*Job Name:* `").append(jobExecution.getJobInstance().getJobName()).append("`\n");
        sb.append("*Job Parameters:* `").append(jobExecution.getJobParameters()).append("`\n");
        sb.append("*Status:* ").append(status).append("\n");
        sb.append("*Start Time:* ").append(formatTime(jobExecution.getStartTime())).append("\n");
        sb.append("*End Time:* ").append(formatTime(jobExecution.getEndTime())).append("\n\n");

        // 실패한 예외 목록
        List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
        if (!exceptions.isEmpty()) {
            sb.append(":warning: *실패한 예외 목록:*\n");
            for (Throwable exception : exceptions) {
                sb.append("  - `").append(exception.getClass().getSimpleName())
                        .append("`: ").append(exception.getMessage()).append("\n");
            }
            sb.append("\n");
        }

        // Step별 통계
        sb.append("*Step별 통계:*\n");
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            sb.append("━━━━━━━━━━━━━━━━━━━━\n");
            sb.append(":arrow_forward: *Step:* `").append(stepExecution.getStepName()).append("`\n");
            sb.append("  • Read Count: ").append(stepExecution.getReadCount()).append("\n");
            sb.append("  • Write Count: ").append(stepExecution.getWriteCount()).append("\n");
            sb.append("  • Skip Count: ").append(stepExecution.getSkipCount()).append("\n");
            sb.append("  • Commit Count: ").append(stepExecution.getCommitCount()).append("\n");
            sb.append("  • Rollback Count: ").append(stepExecution.getRollbackCount()).append("\n");
        }

        return sb.toString();
    }

    private String formatTime(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : "-";
    }

    private void sendSlackMessage(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[Slack 알림 스킵] webhook URL이 설정되지 않았습니다.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = new HashMap<>();
            payload.put("text", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.info("[Slack 알림 전송 완료]");
        } catch (Exception e) {
            log.error("[Slack 알림 전송 실패] error={}", e.getMessage(), e);
        }
    }
}
