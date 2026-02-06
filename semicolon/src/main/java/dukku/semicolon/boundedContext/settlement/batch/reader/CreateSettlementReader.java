package dukku.semicolon.boundedContext.settlement.batch.reader;

import dukku.common.shared.order.dto.ConfirmedOrderItemResponse;
import dukku.semicolon.shared.order.out.OrderApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Step 1: 정산 대상 생성을 위한 Reader 설정
 * - Order BC API 호출하여 전월 구매 확정된 OrderItem 목록 조회
 * - 매월 1일 실행 시, 지난 달 전체 기간(1일~말일)의 확정 건을 조회
 *
 * [API]
 * - GET /api/v1/internal/orders/items/confirmed?startDateTime={}&endDateTime={}
 * - Response: List<ConfirmedOrderItemResponse>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CreateSettlementReader {

    private final OrderApiClient orderApiClient;

    /**
     * 전월 구매 확정된 주문 상품 목록을 조회하는 Reader
     * - 배치 실행일(매월 1일) 기준 전월 1일 00:00:00 ~ 말일 23:59:59
     * - @StepScope: Step 실행 시점에 Bean 생성 (애플리케이션 시작 시점이 아님)
     */
    @Bean
    @StepScope
    public ListItemReader<ConfirmedOrderItemResponse> confirmedOrderItemReader() {
        // 배치 실행일 기준 전월 (매월 1일 실행 시 지난 달 전체 확정 건 조회)
        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime startDateTime = firstDayOfLastMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfLastMonth.atTime(LocalTime.MAX);

        log.info("[Step 1 Reader] 전월 구매 확정 주문 상품 조회 시작. period={} ~ {}, startDateTime={}, endDateTime={}",
                firstDayOfLastMonth, lastDayOfLastMonth, startDateTime, endDateTime);

        // Order BC API 호출
        List<ConfirmedOrderItemResponse> confirmedItems = orderApiClient.findConfirmedItems(startDateTime, endDateTime);

        log.info("[Step 1 Reader] 전월 구매 확정 주문 상품 조회 완료. count={}", confirmedItems.size());

        return new ListItemReader<>(confirmedItems);
    }
}
