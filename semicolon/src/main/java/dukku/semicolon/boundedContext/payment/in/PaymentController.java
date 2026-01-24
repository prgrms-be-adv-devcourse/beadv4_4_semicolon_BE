package dukku.semicolon.boundedContext.payment.in;

import dukku.semicolon.boundedContext.payment.app.PaymentFacade;
import dukku.semicolon.shared.payment.docs.PaymentApiDocs;
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
@PaymentApiDocs.PaymentTag
public class PaymentController {

        private final PaymentFacade paymentFacade;

        /**
         * 결제 요청 (준비)
         *
         * <p>
         * 프론트에서 결제 준비 요청 시 토스 결제창 호출에 필요한 정보 반환
         */
        @PaymentApiDocs.RequestPayment
        @PostMapping("/request")
        public ResponseEntity<PaymentResponse> requestPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentRequest request) {

                PaymentResponse response = paymentFacade.requestPayment(request, idempotencyKey);
                return ResponseEntity.ok(response);
        }

        /**
         * 결제 승인 확정
         *
         * <p>
         * 토스 인증 완료 후 백엔드에서 최종 승인 처리
         */
        @PaymentApiDocs.ConfirmPayment
        @PostMapping("/confirm")
        public ResponseEntity<PaymentConfirmResponse> confirmPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentConfirmRequest request) {

                PaymentConfirmResponse response = paymentFacade.confirmPayment(request, idempotencyKey);
                return ResponseEntity.ok(response);
        }

        /**
         * 결제 내역 조회
         *
         * <p>
         * 결제 상세 정보 조회
         */
        @PaymentApiDocs.GetPaymentResult
        @GetMapping("/result/{paymentId}")
        public ResponseEntity<PaymentResultResponse> getPaymentResult(
                        @PathVariable UUID paymentId) {

                PaymentResultResponse response = paymentFacade.findPaymentResult(paymentId);
                return ResponseEntity.ok(response);
        }

        /**
         * 환불 요청
         *
         * <p>
         * 결제 취소/환불 처리
         */
        @PaymentApiDocs.RefundPayment
        @PostMapping("/refund")
        public ResponseEntity<PaymentRefundResponse> refundPayment(
                        @RequestHeader("Idempotency-Key") String idempotencyKey,
                        @RequestBody @Validated PaymentRefundRequest request) {

                PaymentRefundResponse response = paymentFacade.refundPayment(request, idempotencyKey);
                return ResponseEntity.ok(response);
        }
}
