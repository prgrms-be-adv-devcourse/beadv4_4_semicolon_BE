package dukku.semicolon.boundedContext.order.app;

import dukku.common.global.UserUtil;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.global.exception.ForbiddenException;
import dukku.common.global.exception.NotFoundException;
import dukku.common.shared.order.event.OrderItemCanceledEvent;
import dukku.common.shared.order.event.OrderItemConfirmedEvent;
import dukku.common.shared.order.event.OrderItemRefundRequestedEvent;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateOrderItemStatusUseCase {
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(UUID orderItemUuid, OrderItemStatus newStatus) {
        if (newStatus == null) return;

        OrderItem orderItem = orderItemRepository.findByUuid(orderItemUuid)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 주문 상품입니다."));

        // 1. 권한 검증 (관리자 프리패스, 사용자 엄격 검증)
        checkPermission(orderItem, newStatus);

        // 2. 상태 변경 (엔티티 내부에서 흐름 검증 수행 -> 실패 시 예외 발생)
        orderItem.updateOrderStatus(newStatus);

        // 3. 변경된 상태에 맞는 이벤트 발행 (알림, 정산, 재고 복구 등)
        publishEvent(orderItem, newStatus);

        log.info("주문 상품 상태 변경 완료: uuid={}, status={}", orderItemUuid, newStatus);
    }

    private void checkPermission(OrderItem orderItem, OrderItemStatus newStatus) {
        // 관리자는 모든 권한 허용 (단, 엔티티의 논리적 흐름 검증은 통과해야 함)
        if (UserUtil.isAdmin()) {
            return;
        }

        // 본인 주문 확인
        if (!orderItem.getOrder().getUserUuid().equals(UserUtil.getUserId())) {
            throw new ForbiddenException("주문 수정 권한이 없습니다.");
        }

        // 사용자가 요청할 수 있는 상태인지 확인 (Enum 내 정의된 리스트 체크)
        // ex: 사용자가 갑자기 status를 'SHIPPING(배송중)'으로 바꾸는 해킹 시도 방어
        if (!OrderItemStatus.isUserActionAllowed(newStatus)) {
            throw new ForbiddenException("사용자가 변경할 수 없는 상태입니다.");
        }
    }

    private void publishEvent(OrderItem orderItem, OrderItemStatus newStatus) {
        switch (newStatus) {
            case CANCELED ->
                // 주문 취소: 상품 서비스에 '재고 복구(Increase Stock)' 요청 및 결제 서비스에 환불 로직 트리거
                    eventPublisher.publish(new OrderItemCanceledEvent(orderItem.getUuid()));
            case CONFIRMED ->
                // 구매 확정: 정산 서비스에 '판매자 정산 대기' 등록 및 사용자에게 포인트 적립 트리거
                    eventPublisher.publish(new OrderItemConfirmedEvent(orderItem.getUuid()));
            case REFUND_REQUESTED ->
                // 환불/반품 신청: 판매자에게 '반품 요청 알림' 발송 및 관리자 환불 검토 리스트 진입
                    eventPublisher.publish(new OrderItemRefundRequestedEvent(orderItem.getUuid()));
        }
    }
}