package dukku.semicolon.boundedContext.payment.in;

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

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    /**
     * 결제 요청
     */
    @PostMapping("/request")
    public ResponseEntity<PaymentResponse> requestPayment(
            @RequestBody @Validated PaymentRequest request) {
        // TODO: 구현 예정
        return ResponseEntity.ok().build();
    }

    /**
     * 결제 승인 확정
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestBody @Validated PaymentConfirmRequest request) {
        // TODO: 구현 예정
        return ResponseEntity.ok().build();
    }

    /**
     * 결제 내역 조회
     */
    @GetMapping("/result/{paymentId}")
    public ResponseEntity<PaymentResultResponse> getPaymentResult(
            @PathVariable String paymentId) {
        // TODO: 구현 예정
        return ResponseEntity.ok().build();
    }

    /**
     * 환불
     */
    @PostMapping("/refund")
    public ResponseEntity<PaymentRefundResponse> refundPayment(
            @RequestBody @Validated PaymentRefundRequest request) {
        // TODO: 구현 예정
        return ResponseEntity.ok().build();
    }
}
