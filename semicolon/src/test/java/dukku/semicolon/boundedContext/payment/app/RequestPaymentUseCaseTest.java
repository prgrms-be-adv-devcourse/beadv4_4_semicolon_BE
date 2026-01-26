package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import dukku.semicolon.shared.payment.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 결제 요청(준비) UseCase 단위 테스트
 */
class RequestPaymentUseCaseTest {

        private PaymentSupport paymentSupport;
        private RequestPaymentUseCase requestPaymentUseCase;
        private UUID userUuid;

        @BeforeEach
        void setUp() {
                paymentSupport = mock(PaymentSupport.class);
                requestPaymentUseCase = new RequestPaymentUseCase(paymentSupport);
                userUuid = UUID.randomUUID();

                // savePayment 호출 시 인자로 받은 Payment 객체를 그대로 반환하도록 설정
                when(paymentSupport.savePayment(any(Payment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("예치금 분배 테스트: productId ASC 정렬 순서대로 할당되어야 한다")
        void depositDistributionTest() {
                try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
                        userUtil.when(UserUtil::getUserId).thenReturn(userUuid);

                        // Given: 두 개의 주문 상품 준비 (아이템 2의 productId가 더 작음)
                        UUID orderUuid = UUID.randomUUID();
                        UUID itemUuid1 = UUID.randomUUID();
                        UUID itemUuid2 = UUID.randomUUID();

                        PaymentRequest.PaymentRequestItem item1 = PaymentRequest.PaymentRequestItem.builder()
                                        .orderItemUuid(itemUuid1)
                                        .productId(2) // 더 큰 ID
                                        .productName("상품2")
                                        .price(10000L)
                                        .sellerUuid(UUID.randomUUID())
                                        .build();

                        PaymentRequest.PaymentRequestItem item2 = PaymentRequest.PaymentRequestItem.builder()
                                        .orderItemUuid(itemUuid2)
                                        .productId(1) // 더 작은 ID
                                        .productName("상품1")
                                        .price(10000L)
                                        .sellerUuid(UUID.randomUUID())
                                        .build();

                        // 총 2만원 주문, 그 중 예치금을 1.5만원 사용한다고 가정
                        PaymentRequest request = PaymentRequest.builder()
                                        .orderUuid(orderUuid)
                                        .orderName("테스트 주문")
                                        .amounts(PaymentRequest.Amounts.builder()
                                                        .itemsTotalAmount(20000L)
                                                        .couponDiscountAmount(0L)
                                                        .finalPayAmount(20000L)
                                                        .depositUseAmount(15000L)
                                                        .pgPayAmount(5000L)
                                                        .build())
                                        .items(Arrays.asList(item1, item2)) // 전송 순서는 [2, 1]
                                        .build();

                        // When: 결제 요청 실행
                        requestPaymentUseCase.execute(request, "idempotency-key");

                        // Then: 결제 상품들에 예치금이 productId 순서에 따라 분배되었는지 검증
                        verify(paymentSupport).savePayment(argThat(payment -> {
                                List<PaymentOrderItem> items = payment.getItems();
                                // 정렬 결과: item2(productId: 1)가 첫 번째, item1(productId: 2)이 두 번째여야 함
                                PaymentOrderItem firstItem = items.stream()
                                                .filter(i -> i.getOrderItemUuid().equals(itemUuid2))
                                                .findFirst().get();
                                PaymentOrderItem secondItem = items.stream()
                                                .filter(i -> i.getOrderItemUuid().equals(itemUuid1))
                                                .findFirst().get();

                                // 첫 번째 아이템(item2)에 10,000원 전액 할당
                                // 두 번째 아이템(item1)에 남은 5,000원 할당 확인
                                return firstItem.getPaymentDeposit() == 10000L &&
                                                secondItem.getPaymentDeposit() == 5000L;
                        }));
                }
        }
}
