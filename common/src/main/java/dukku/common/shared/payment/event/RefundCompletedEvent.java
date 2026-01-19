package dukku.common.shared.payment.event;

import dukku.common.shared.order.type.OrderStatus;

import java.util.UUID;

// 환불 프로세스는 추후 본 프로젝트에서 반영.
public record RefundCompletedEvent(UUID orderUuid, OrderStatus status, int refundAmount) {}
