package dukku.semicolon.shared.order.dto;

import dukku.common.shared.order.type.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderUpdateRequest {

    @Getter
    @NoArgsConstructor
    public static class ShippingInfo {
        private String address;
        private String recipient;
        private String contactNumber;
    }

    @Getter
    @NoArgsConstructor
    public static class Refund {
        private int refundedAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class Status {
        private OrderStatus status;
    }
}
