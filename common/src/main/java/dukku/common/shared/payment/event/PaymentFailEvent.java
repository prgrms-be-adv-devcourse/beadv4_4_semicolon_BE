package dukku.common.shared.payment.event;

import java.util.UUID;

// 결제 서비스에서 결제 실패 시 이벤트 발생
public record PaymentFailEvent(UUID orderUuid) {
}
