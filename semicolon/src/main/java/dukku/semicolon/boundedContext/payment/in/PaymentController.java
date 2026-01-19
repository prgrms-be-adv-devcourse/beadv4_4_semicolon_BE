package dukku.semicolon.boundedContext.payment.in;

import dukku.semicolon.boundedContext.payment.entity.enums.PaymentStatus;
import dukku.semicolon.shared.payment.dto.PaymentConfirmRequest;
import dukku.semicolon.shared.payment.dto.PaymentConfirmResponse;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import dukku.semicolon.shared.payment.dto.PaymentRefundResponse;
import dukku.semicolon.shared.payment.dto.PaymentRequest;
import dukku.semicolon.shared.payment.dto.PaymentResponse;
import dukku.semicolon.shared.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 결제 API 컨트롤러
 *
 * <p>
 * 결제 요청, 승인, 조회, 환불 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

        /**
         * 결제 요청 (준비)
         *
         * <p>
         * 프론트에서 결제 준비 요청 시 토스 결제창 호출에 필요한 정보 반환
         */
        @PostMapping("/request")
        public ResponseEntity<PaymentResponse> requestPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentRequest request) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                UUID paymentUuid = UUID.randomUUID();
                String tossOrderId = "TOSS_" + paymentUuid.toString().substring(0, 8) + "_" +
                                OffsetDateTime.now().toLocalDate().toString().replace("-", "");

                PaymentResponse response = PaymentResponse.builder()
                                .paymentUuid(paymentUuid)
                                .status(PaymentStatus.PENDING)
                                .toss(PaymentResponse.TossInfo.builder()
                                                .orderId(tossOrderId)
                                                .amount(request.getAmounts().getPgPayAmount())
                                                .orderName(request.getOrderName())
                                                .successUrl("https://localhost:3000/payments/success?paymentUuid="
                                                                + paymentUuid) // 더미 url
                                                .failUrl("https://localhost:3000/payments/fail?paymentUuid="
                                                                + paymentUuid) // 더미url
                                                .build())
                                .amounts(PaymentResponse.ResponseAmounts.builder()
                                                .finalPayAmount(request.getAmounts().getFinalPayAmount())
                                                .depositUseAmount(request.getAmounts().getDepositUseAmount())
                                                .pgPayAmount(request.getAmounts().getPgPayAmount())
                                                .build())
                                .createdAt(OffsetDateTime.now())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 결제 승인 확정
         *
         * <p>
         * 토스 인증 완료 후 백엔드에서 최종 승인 처리
         */
        @PostMapping("/confirm")
        public ResponseEntity<PaymentConfirmResponse> confirmPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentConfirmRequest request) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                PaymentConfirmResponse response = PaymentConfirmResponse.builder()
                                .success(true)
                                .code("PAYMENT_CONFIRMED")
                                .message("결제가 승인되었습니다.")
                                .data(PaymentConfirmResponse.PaymentConfirmData.builder()
                                                .paymentUuid(request.getPaymentUuid())
                                                .status(PaymentStatus.DONE)
                                                .approvedAt(OffsetDateTime.now())
                                                .toss(PaymentConfirmResponse.TossInfo.builder()
                                                                .orderId(request.getToss().getOrderId())
                                                                .paymentKey(request.getToss().getPaymentKey())
                                                                .build())
                                                .amounts(PaymentConfirmResponse.AmountInfo.builder()
                                                                .finalPayAmount(request.getToss().getAmount())
                                                                .depositUseAmount(0)
                                                                .pgPayAmount(request.getToss().getAmount())
                                                                .build())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 결제 내역 조회
         *
         * <p>
         * 결제 상세 정보 조회
         */
        @GetMapping("/result/{paymentId}")
        public ResponseEntity<PaymentResultResponse> getPaymentResult(
                        @PathVariable UUID paymentId) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                PaymentResultResponse response = PaymentResultResponse.builder()
                                .success(true)
                                .code("PAYMENT_RESULT_RETRIEVED")
                                .message("결제 내역을 조회했습니다.")
                                .data(PaymentResultResponse.PaymentResultData.builder()
                                                .paymentId(paymentId)
                                                .orderUuid(UUID.randomUUID())
                                                .status(PaymentStatus.DONE)
                                                .amounts(PaymentResultResponse.AmountInfo.builder()
                                                                .totalAmount(15000)
                                                                .couponDiscountAmount(1500)
                                                                .depositUseAmount(4500)
                                                                .pgPayAmount(9000)
                                                                .finalPayAmount(13500)
                                                                .build())
                                                .createdAt(OffsetDateTime.now().minusMinutes(10))
                                                .approvedAt(OffsetDateTime.now().minusMinutes(8))
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 환불 요청
         *
         * <p>
         * 결제 취소/환불 처리
         */
        @PostMapping("/refund")
        public ResponseEntity<PaymentRefundResponse> refundPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentRefundRequest request) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                PaymentRefundResponse response = PaymentRefundResponse.builder()
                                .success(true)
                                .code("REFUND_REQUESTED")
                                .message("환불 요청이 접수되었습니다.")
                                .data(PaymentRefundResponse.RefundData.builder()
                                                .refundId(UUID.randomUUID())
                                                .paymentId(request.getPaymentId())
                                                .orderUuid(request.getOrderUuid())
                                                .status(PaymentStatus.CANCELED)
                                                .amounts(PaymentRefundResponse.RefundAmountInfo.builder()
                                                                .requestedRefundAmount(request.getRefundAmount())
                                                                .depositRefundAmount(0)
                                                                .pgRefundAmount(request.getRefundAmount())
                                                                .build())
                                                .pg(PaymentRefundResponse.PgInfo.builder()
                                                                .provider("TOSS_PAYMENTS")
                                                                .tossOrderId("TOSS_" + request.getPaymentId().toString()
                                                                                .substring(0, 8))
                                                                .cancelTransactionKey("CANCEL_TXN_" + UUID.randomUUID()
                                                                                .toString().substring(0, 8))
                                                                .build())
                                                .createdAt(OffsetDateTime.now())
                                                .completedAt(OffsetDateTime.now())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }
}
