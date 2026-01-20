package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.shared.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * 결제 내역 조회 UseCase
 *
 * <p>
 * 결제 UUID로 단건 조회하여 상세 정보 반환
 */
@Component
@RequiredArgsConstructor
public class FindPaymentUseCase {

        private final PaymentSupport support;

        /**
         * 결제 내역 단건 조회
         *
         * @param paymentUuid 결제 UUID
         * @return 결제 상세 정보
         */
        @Transactional(readOnly = true)
        public PaymentResultResponse execute(UUID paymentUuid) {
                Payment payment = support.findPaymentByUuid(paymentUuid);
                return buildResponse(payment);
        }

        private PaymentResultResponse buildResponse(Payment payment) {
                // approvedAt LocalDateTime → OffsetDateTime 변환
                OffsetDateTime approvedAtOffset = payment.getApprovedAt() != null
                                ? payment.getApprovedAt().atOffset(ZoneOffset.ofHours(9))
                                : null;

                // createdAt도 LocalDateTime → OffsetDateTime 변환
                OffsetDateTime createdAtOffset = payment.getCreatedAt() != null
                                ? payment.getCreatedAt().atOffset(ZoneOffset.ofHours(9))
                                : null;

                return PaymentResultResponse.builder()
                                .success(true)
                                .code("PAYMENT_RESULT_RETRIEVED")
                                .message("결제 내역을 조회했습니다.")
                                .data(PaymentResultResponse.PaymentResultData.builder()
                                                .paymentId(payment.getUuid())
                                                .orderUuid(payment.getPaymentOrder().getUuid())
                                                .status(payment.getPaymentStatus())
                                                .amounts(PaymentResultResponse.AmountInfo.builder()
                                                                .totalAmount(payment.getAmount())
                                                                .couponDiscountAmount(payment.getPaymentCouponTotal())
                                                                .depositUseAmount(payment.getPaymentDeposit())
                                                                .pgPayAmount(payment.getAmountPg())
                                                                .finalPayAmount(payment.getAmount())
                                                                .build())
                                                .createdAt(createdAtOffset)
                                                .approvedAt(approvedAtOffset)
                                                .build())
                                .build();
        }
}
