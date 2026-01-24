package dukku.semicolon.boundedContext.payment.app;

import dukku.common.shared.payment.type.PaymentType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.semicolon.boundedContext.deposit.app.IncreaseDepositUseCase;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.out.TossPaymentClient;
import dukku.semicolon.boundedContext.payment.out.PaymentRepository;
import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PaymentIntegrationTest {

        @Autowired
        private RefundPaymentUseCase refundPaymentUseCase;

        @Autowired
        private CompensatePaymentUseCase compensatePaymentUseCase;

        @Autowired
        private PaymentSupport paymentSupport;

        @MockitoSpyBean
        private PaymentRepository paymentRepository;

        @MockitoSpyBean
        private TossPaymentClient tossPaymentClient;

        @MockitoBean
        private IncreaseDepositUseCase increaseDepositUseCase;

        private Payment testPayment;
        private final UUID userUuid = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                // Given: 테스트용 결제 데이터 생성
                testPayment = Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(UUID.randomUUID())
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("test-payment-key")
                                .tossOrderId("test-order-id")
                                .refundTotal(0L)
                                .build();

                paymentSupport.savePayment(testPayment);
        }

        @Test
        @DisplayName("전체 환불 성공 시나리오: PG 취소 후 예치금 복구가 순차적으로 일어나야 한다")
        void fullRefundSuccessTest() {
                // Mocking: PG 취소 성공 응답 (statusCode=200)
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("statusCode", 200);
                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(successResponse);

                // When: 전액 환불 요청
                PaymentRefundRequest request = PaymentRefundRequest.builder()
                                .paymentId(testPayment.getUuid())
                                .refundAmount(20000L)
                                .reason("단순 변심")
                                .build();

                refundPaymentUseCase.execute(request, "idempotency-key");

                // Then:
                // 1. PG 취소 호출 확인 (5000원)
                verify(tossPaymentClient).cancel(eq("test-payment-key"),
                                argThat(map -> map.get("cancelAmount").equals(5000L)));

                // 2. 예치금 복구 호출 확인 (15000원)
                verify(increaseDepositUseCase).increase(eq(userUuid), eq(15000L), any(), any());

                // 3. 결제 상태 확인
                Payment updatedPayment = paymentSupport.findPaymentByUuid(testPayment.getUuid());
                assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
                assertThat(updatedPayment.getAmountPg()).isEqualTo(0L);
                assertThat(updatedPayment.getPaymentDeposit()).isEqualTo(0L);
                assertThat(updatedPayment.getRefundTotal()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("PG 취소 실패 시나리오: PG사 오류 발생 시 ROLLBACK_FAILED 상태가 되어야 하고 예치금 복구는 건너뛴다")
        void pgCancelFailureTest() {
                // Mocking: PG 취소 실패 응답 (statusCode=400)
                Map<String, Object> failureResponse = new HashMap<>();
                failureResponse.put("statusCode", 400);
                failureResponse.put("message", "PG사 내부 오류");
                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(failureResponse);

                // When & Then: PG 취소 실패 시 예외가 던져져야 함
                PaymentRefundRequest request = PaymentRefundRequest.builder()
                                .paymentId(testPayment.getUuid())
                                .refundAmount(20000L)
                                .reason("단순 변심")
                                .build();

                assertThatThrownBy(() -> refundPaymentUseCase.execute(request, "idempotency-key"))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("TOSS_CANCEL_FAILED");

                // Then:
                // 1. 예치금 복구는 호출되지 않아야 함
                verify(increaseDepositUseCase, never()).increase(any(), any(), any(), any());

                // 2. 결제 상태가 ROLLBACK_FAILED 인지 확인 (rollbackFailedStatus 메서드 호출 결과)
                Payment updatedPayment = paymentSupport.findPaymentByUuid(testPayment.getUuid());
                assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.ROLLBACK_FAILED);
        }

        @Test
        @DisplayName("단계적 부분 환불 테스트 - 예치금 우선 환불 정책 및 상태 전이 검증")
        void partialRefundStepByStepTest() {
                // Given: 혼합 결제 생성 (PG 5,000 + 예치금 15,000 = 총 20,000)
                Payment payment = Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(UUID.randomUUID())
                                .amount(20000L)
                                .amountPgOrigin(5000L)
                                .amountPg(5000L)
                                .paymentDepositOrigin(15000L)
                                .paymentDeposit(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("partial-test-key")
                                .tossOrderId("toss-order-partial")
                                .refundTotal(0L)
                                .build();
                paymentSupport.savePayment(payment);

                // Step 1: 8,000원 환불 요청 (예치금 15,000 중 8,000 차감 예상)
                PaymentRefundRequest req1 = PaymentRefundRequest.builder()
                                .paymentId(payment.getUuid())
                                .refundAmount(8000L)
                                .reason("부분 취소 1")
                                .build();

                refundPaymentUseCase.execute(req1, "idempotency-1");

                // Then 1: 예치금 8,000 복구, PG 취소 없음
                Payment p1 = paymentSupport.findPaymentByUuid(payment.getUuid());
                assertThat(p1.getPaymentDeposit()).isEqualTo(7000L); // 15,000 - 8,000
                assertThat(p1.getAmountPg()).isEqualTo(5000L); // 그대로
                assertThat(p1.getRefundTotal()).isEqualTo(8000L);
                assertThat(p1.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELED);
                verify(tossPaymentClient, never()).cancel(anyString(), anyMap());
                verify(increaseDepositUseCase, times(1)).increase(eq(userUuid), eq(8000L), any(), any());

                // Step 2: 추가 10,000원 환불 요청 (남은 예치금 7,000 전액 + PG 3,000 취소 예상)
                PaymentRefundRequest req2 = PaymentRefundRequest.builder()
                                .paymentId(payment.getUuid())
                                .refundAmount(10000L)
                                .reason("부분 취소 2")
                                .build();

                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(Map.of("statusCode", 200));

                refundPaymentUseCase.execute(req2, "idempotency-2");

                // Then 2: 예치금 7,000 복구(잔액 0), PG 3,000 취소(잔액 2,000)
                Payment p2 = paymentSupport.findPaymentByUuid(payment.getUuid());
                assertThat(p2.getPaymentDeposit()).isEqualTo(0L);
                assertThat(p2.getAmountPg()).isEqualTo(2000L); // 5,000 - 3,000
                assertThat(p2.getRefundTotal()).isEqualTo(18000L); // 8,000 + 10,000
                assertThat(p2.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELED);

                // PG 취소 호출 확인
                verify(tossPaymentClient, times(1)).cancel(eq("partial-test-key"),
                                argThat(map -> map.get("cancelAmount").equals(3000L)));

                // 예치금 복구 호출 확인 (두 번째 호출 7,000원)
                verify(increaseDepositUseCase).increase(eq(userUuid), eq(7000L), any(), any());

                // Step 3: 마지막 2,000원 환불 요청 (남은 PG 2,000 전액 취소 예상 -> 상태 CANCELED)
                PaymentRefundRequest req3 = PaymentRefundRequest.builder()
                                .paymentId(payment.getUuid())
                                .refundAmount(2000L)
                                .reason("최종 취소")
                                .build();

                refundPaymentUseCase.execute(req3, "idempotency-3");

                Payment p3 = paymentSupport.findPaymentByUuid(payment.getUuid());
                assertThat(p3.getPaymentDeposit()).isEqualTo(0L);
                assertThat(p3.getAmountPg()).isEqualTo(0L);
                assertThat(p3.getRefundTotal()).isEqualTo(20000L);
                assertThat(p3.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        }

        @Test
        @DisplayName("보상 트랜잭션 성공 테스트: CompensatePaymentUseCase가 RefundPaymentUseCase를 정상적으로 호출해야 한다")
        void compensatePaymentSuccessTest() {
                // Given: 결제 완료 상태 데이터 (PG 5,000 + 예치금 15,000)
                // testPayment(setUp에서 생성됨) 활용

                // Mocking: PG 취소 성공
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("statusCode", 200);
                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(successResponse);

                // When: 보상 트랜잭션 실행
                compensatePaymentUseCase.execute(testPayment.getOrderUuid(), "시스템 오류 발생");

                // Then:
                // 1. PG 취소 호출 확인
                verify(tossPaymentClient).cancel(eq("test-payment-key"), anyMap());

                // 2. 예치금 복구 호출 확인
                verify(increaseDepositUseCase).increase(eq(userUuid), eq(15000L), any(), any());

                // 3. 결제 상태 확인
                Payment updatedPayment = paymentSupport.findPaymentByUuid(testPayment.getUuid());
                assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
                assertThat(updatedPayment.getRefundTotal()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("재시도(Retry) 작동 검증: 500 에러 발생 시 설정된 횟수만큼 재시도해야 한다")
        void retryVerificationTest() {
                // Given
                // 전액 환불 요청 (예치금 15,000 소진 후 PG 5,000 취소 발생)
                PaymentRefundRequest request = PaymentRefundRequest.builder()
                                .paymentId(testPayment.getUuid())
                                .refundAmount(20000L)
                                .reason("재시도 테스트")
                                .build();

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("statusCode", 200);

                // thenThrow를 사용해야 @Retryable 프록시가 예외를 감지하고 재시도함
                when(tossPaymentClient.cancel(anyString(), anyMap()))
                                .thenThrow(new RuntimeException("Retry-1"))
                                .thenThrow(new RuntimeException("Retry-2"))
                                .thenReturn(successResponse);

                // When
                refundPaymentUseCase.execute(request, "retry-test-key");

                // Then
                // 총 3번 호출되었는지 확인
                verify(tossPaymentClient, times(3)).cancel(eq("test-payment-key"), anyMap());

                Payment updatedPayment = paymentSupport.findPaymentByUuid(testPayment.getUuid());
                assertThat(updatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        }

        @Test
        @DisplayName("DB 저장 재시도(Retry) 검증: 일시적 DB 장애 발생 시 savePayment가 재시도 되어야 한다")
        void dbRetryVerificationTest() {
                // Given
                // Repository가 첫 2번은 DataAccessException 던지도록 설정
                doThrow(new org.springframework.dao.RecoverableDataAccessException("DB Connection Fail 1"))
                                .doThrow(new org.springframework.dao.RecoverableDataAccessException(
                                                "DB Connection Fail 2"))
                                .doCallRealMethod()
                                .when(paymentRepository).save(any());

                // When
                paymentSupport.savePayment(testPayment);

                // Then
                // Repository.save()가 총 3번 호출되었는지 확인
                verify(paymentRepository, times(3)).save(any());
        }
}
