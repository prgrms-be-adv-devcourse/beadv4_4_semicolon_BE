package dukku.semicolon.shared.order.dto;

import dukku.common.shared.order.type.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminOrderSearchCondition(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate,

        OrderStatus status,

        UUID userUuid,      // 특정 회원의 주문 내역 조회용

        String orderUuid    // 주문 번호로 검색
) {}
