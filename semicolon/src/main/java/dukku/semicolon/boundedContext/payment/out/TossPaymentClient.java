package dukku.semicolon.boundedContext.payment.out;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 토스페이먼츠 API 통신 클라이언트
 * 
 * <p>
 * 공식 샘플의 핵심 로직(통신 및 JSON 처리)이 이 클래스에 집중됩니다.
 * 공식 예제와 달리 JSON은 Jackson를 사용하여 처리합니다. (별도 의존성을 추가하지 않기 위함)
 */
@Slf4j
@Component
public class TossPaymentClient {

    private final ObjectMapper objectMapper;

    @Value("${toss.api.secret-key}")
    private String widgetSecretKey;

    public TossPaymentClient() {
        this.objectMapper = new ObjectMapper();
        // 날짜/시간 처리를 위한 모듈 등록 및 설정
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    /**
     * 토스 결제 승인 API 호출
     * 
     * @param tossRequestBody 토스 전용 요청 바디 (paymentKey, orderId, amount)
     * @return 토스 API 응답 (statusCode 포함)
     */
    /**
     * 토스 결제 승인 API 호출
     * 
     * @param tossRequestBody 토스 전용 요청 바디 (paymentKey, orderId, amount)
     * @return 토스 API 응답 (statusCode 포함)
     */
    public Map<String, Object> confirm(Map<String, Object> tossRequestBody) {
        return sendRequest(TOSS_CONFIRM_URL, tossRequestBody);
    }

    /**
     * 토스 결제 취소 API 호출
     *
     * @param paymentKey 결제 키
     * @param cancelBody 취소 사유 및 금액 정보
     * @return 토스 API 응답
     */
    public Map<String, Object> cancel(String paymentKey, Map<String, Object> cancelBody) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        return sendRequest(url, cancelBody);
    }

    private Map<String, Object> sendRequest(String urlStr, Map<String, Object> requestBody) {
        try {
            String authorizations = Base64.getEncoder()
                    .encodeToString((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));

            URI uri = URI.create(urlStr);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 바디 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = objectMapper.writeValueAsBytes(requestBody);
                os.write(input, 0, input.length);
            }

            // 응답 읽기
            int responseCode = connection.getResponseCode();
            InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            String responseBody = sb.toString();
            log.info("[Toss API Response] code={}, body={}", responseCode, responseBody);

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
            responseMap.put("statusCode", responseCode);

            return responseMap;

        } catch (IOException e) {
            log.error("[Toss API Call Failed]", e);
            throw new RuntimeException("TOSS_CONNECTION_FAILED", e);
        }
    }
}
