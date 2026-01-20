package dukku.common.shared.order.type;

public enum OrderItemStatus {
    // 결제
    PAYMENT_COMPLETED,      // 결제 완료

    // 배송
    PREPARING_SHIPMENT,     // 배송 준비 중
    SHIPPED,                // 배송 중
    DELIVERED,              // 배송 완료

    // 구매 확정
    CONFIRM_PENDING,        // 구매 확정 대기
    CONFIRMED,              // 구매 확정 완료

    // 취소
    CANCEL_REQUESTED,       // 취소 요청
    CANCEL_IN_PROGRESS,     // 취소 처리 중
    CANCELED,              // 취소 완료

    // 환불
    REFUND_REQUESTED,       // 환불 요청
    REFUND_IN_PROGRESS,     // 환불 진행 중
    REFUND_COMPLETED;        // 환불 완료

    public boolean canChangeShippingInfo() {
        return this == PAYMENT_COMPLETED || this == PREPARING_SHIPMENT;
    }

    public static boolean isUserActionAllowed(OrderItemStatus status) {
        return status == CONFIRMED
                || status == CANCEL_REQUESTED
                || status == REFUND_REQUESTED;
    }
}
