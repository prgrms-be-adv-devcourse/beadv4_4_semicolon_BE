package dukku.semicolon.boundedContext.payment.app;

import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.common.shared.payment.type.PaymentType;
import dukku.common.shared.order.type.OrderItemStatus;
import dukku.common.shared.order.type.OrderStatus;
import dukku.common.global.eventPublisher.EventPublisher;
import dukku.semicolon.boundedContext.deposit.app.DeductDepositForPaymentUseCase;
import dukku.semicolon.boundedContext.deposit.app.DepositSupport;
import dukku.semicolon.boundedContext.deposit.app.FindDepositUseCase;
import dukku.semicolon.boundedContext.deposit.app.IncreaseDepositUseCase;
import dukku.semicolon.boundedContext.deposit.entity.Deposit;
import dukku.semicolon.boundedContext.deposit.entity.DepositHistory;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import dukku.semicolon.boundedContext.order.app.OrderSupport;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.out.TossPaymentClient;
import dukku.semicolon.global.SystemDepositInitData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
                "spring.datasource.hikari.connection-timeout=3000",
                "spring.datasource.hikari.idle-timeout=30000",
                "spring.datasource.hikari.max-lifetime=1800000"
})
@Import(PaymentDepositSagaIntegrationTest.AsyncTestConfig.class)
class PaymentDepositSagaIntegrationTest {

        @Autowired
        private DeductDepositForPaymentUseCase deductDepositForPaymentUseCase;

        @Autowired
        private FindDepositUseCase findDepositUseCase;

        @Autowired
        private DepositSupport depositSupport;

        @Autowired
        private PaymentSupport paymentSupport;

        @Autowired
        private OrderSupport orderSupport;

        @Autowired
        private EventPublisher eventPublisher;

        @Autowired
        private PlatformTransactionManager transactionManager;

        @MockitoBean
        private TossPaymentClient tossPaymentClient;

        @MockitoBean
        private IncreaseDepositUseCase increaseDepositUseCase;

        @MockitoBean(name = "initUsers")
        private CommandLineRunner initUsers;

        @MockitoBean(name = "initSystemDeposit")
        private CommandLineRunner initSystemDeposit;

        @MockitoBean(name = "initProducts")
        private CommandLineRunner initProducts;

        @MockitoBean(name = "initCategories")
        private CommandLineRunner initCategories;

        @Test
        @DisplayName("SAGA 실패: 예치금 차감 중간 실패 시 부분 차감은 롤백되고 결제 보상이 수행된다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rollbackDeductionAndCompensatePaymentWhenDeductionFailsMidway() {
                // given: 결제 완료 상태 + 사용자 예치금은 7,000원만 보유(8,000원 차감 시 중간 실패 유도)
                UUID userUuid = UUID.randomUUID();
                UUID orderUuid = UUID.randomUUID();

                Payment savedPayment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(orderUuid)
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("saga-test-payment-key")
                                .tossOrderId("saga-test-order-id")
                                .refundTotal(0L)
                                .build());

                seedUserDeposit(userUuid, 7000L);

                List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages = List.of(
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 3000L),
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 5000L));

                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(Map.of("statusCode", 200));

                // when: 상품별 예치금 3,000 + 5,000 차감을 실행한다
                deductDepositForPaymentUseCase.execute(
                                userUuid,
                                8000L,
                                orderUuid,
                                savedPayment.getUuid(),
                                itemDepositUsages);

                // then: 부분 차감은 롤백되고, 결제 보상(PG 취소 + FAILED 전이)이 수행된다
                Payment compensatedPayment = waitForPaymentStatus(savedPayment.getUuid(), PaymentStatus.FAILED,
                                Duration.ofSeconds(3));
                Deposit userDeposit = findDepositUseCase.findOrCreate(userUuid);
                List<DepositHistory> userHistories = depositSupport.findHistoriesByUserUuid(userUuid);

                assertThat(userDeposit.getBalance()).isEqualTo(7000L);
                assertThat(userHistories).hasSize(1);
                assertThat(userHistories.get(0).getType()).isEqualTo(DepositHistoryType.CHARGE);

                // [SAGA BUG 확인 지점]
                // 기대: 예치금 차감 실패 시 보상 트랜잭션이 실행되어 FAILED 상태여야 함
                // 실제: CompensatePaymentUseCase가 호출되지 않거나 이벤트가 유실되어 DONE 상태로 유지됨
                // 원인: DeductDepositForPaymentUseCase의 REQUIRES_NEW 트랜잭션이 예외를 흡수하고 커밋되면서,
                // AFTER_COMMIT 리스너가 트리거되지 않거나 비동기 레이스 컨디션 발생
                assertThat(compensatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
                assertThat(compensatedPayment.getAmountPg()).isEqualTo(0L);
                assertThat(compensatedPayment.getPaymentDeposit()).isEqualTo(0L);
                assertThat(compensatedPayment.getRefundTotal()).isEqualTo(compensatedPayment.getAmount());
                assertThat(paymentSupport.hasHistoryType(compensatedPayment.getId(), PaymentHistoryType.PAYMENT_FAILED))
                                .isTrue();
                verify(tossPaymentClient).cancel(eq("saga-test-payment-key"),
                                argThat(body -> ((Number) body.get("cancelAmount")).longValue() == 5000L));
        }

        @Test
        @DisplayName("SAGA 성공: 예치금 차감이 완료되면 결제는 DONE을 유지하고 보상은 실행되지 않는다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void completeDeductionWithoutCompensationWhenBalanceIsEnough() {
                // given: 결제 완료 상태 + 사용자 예치금 10,000원(8,000원 차감 가능)
                UUID userUuid = UUID.randomUUID();
                UUID orderUuid = UUID.randomUUID();

                Payment savedPayment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(orderUuid)
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("saga-success-payment-key")
                                .tossOrderId("saga-success-order-id")
                                .refundTotal(0L)
                                .build());

                seedUserDeposit(userUuid, 10000L);

                List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages = List.of(
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 3000L),
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 5000L));

                // when: 상품별 예치금 차감을 실행한다
                deductDepositForPaymentUseCase.execute(
                                userUuid,
                                8000L,
                                orderUuid,
                                savedPayment.getUuid(),
                                itemDepositUsages);

                // then: 결제 상태는 DONE 유지, 예치금은 차감, 시스템 예치금은 증가, PG 보상 취소는 호출되지 않는다
                Payment paymentAfter = paymentSupport.findPaymentByUuid(savedPayment.getUuid());
                Deposit userDeposit = findDepositUseCase.findOrCreate(userUuid);
                List<DepositHistory> userHistories = depositSupport.findHistoriesByUserUuid(userUuid);

                assertThat(paymentAfter.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
                assertThat(userDeposit.getBalance()).isEqualTo(2000L);
                assertThat(userHistories).hasSize(3);
                assertThat(userHistories.stream().map(DepositHistory::getType))
                                .containsExactlyInAnyOrder(DepositHistoryType.CHARGE, DepositHistoryType.USE,
                                                DepositHistoryType.USE);
                verify(increaseDepositUseCase).increase(
                                eq(SystemDepositInitData.SYSTEM_USER_UUID),
                                eq(8000L),
                                eq(DepositHistoryType.CHARGE),
                                eq(orderUuid));
                verify(tossPaymentClient, never()).cancel(anyString(), anyMap());
        }

        @Test
        @DisplayName("SAGA 보상 실패: PG 취소가 실패하면 결제는 ROLLBACK_FAILED로 전이된다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void markRollbackFailedWhenCompensationCancelFails() {
                // given: 결제 완료 상태 + 예치금 차감 중간 실패가 발생하도록 구성, PG 취소는 500 응답으로 실패시킨다
                UUID userUuid = UUID.randomUUID();
                UUID orderUuid = UUID.randomUUID();

                Payment savedPayment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(orderUuid)
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("saga-compensation-fail-payment-key")
                                .tossOrderId("saga-compensation-fail-order-id")
                                .refundTotal(0L)
                                .build());

                seedUserDeposit(userUuid, 7000L);
                List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages = List.of(
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 3000L),
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 5000L));

                when(tossPaymentClient.cancel(anyString(), anyMap()))
                                .thenReturn(Map.of("statusCode", 500, "message", "pg cancel failed"));

                // when: 예치금 차감을 실행하여 보상 경로로 진입시킨다
                deductDepositForPaymentUseCase.execute(
                                userUuid,
                                8000L,
                                orderUuid,
                                savedPayment.getUuid(),
                                itemDepositUsages);

                // then: 결제는 ROLLBACK_FAILED로 기록되고 PAYMENT_ROLLBACK_FAILED 이력이 생성된다
                Payment rollbackFailedPayment = waitForPaymentStatus(
                                savedPayment.getUuid(),
                                PaymentStatus.ROLLBACK_FAILED,
                                Duration.ofSeconds(3));

                // [SAGA BUG 확인 지점]
                // 기대: PG 취소 실패 시 ROLLBACK_FAILED 상태여야 함
                // 원인: 보상 트랜잭션(CompensatePaymentUseCase) 진입 자체가 실패하면 결제는 여전히 DONE 상태임
                assertThat(rollbackFailedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.ROLLBACK_FAILED);
                assertThat(paymentSupport.hasHistoryType(rollbackFailedPayment.getId(),
                                PaymentHistoryType.PAYMENT_ROLLBACK_FAILED))
                                .isTrue();
                verify(tossPaymentClient).cancel(eq("saga-compensation-fail-payment-key"),
                                argThat(body -> ((Number) body.get("cancelAmount")).longValue() == 5000L));
        }

        @Test
        @DisplayName("SAGA 실패: 예치금이 처음부터 부족하면 차감 이력 없이 결제 보상만 수행된다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void compensateWithoutAnyDeductionWhenDepositIsInsufficientFromStart() {
                // given: 결제 완료 상태 + 사용자 예치금 2,000원(첫 차감 3,000원부터 실패)
                UUID userUuid = UUID.randomUUID();
                UUID orderUuid = UUID.randomUUID();

                Payment savedPayment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(orderUuid)
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("saga-insufficient-payment-key")
                                .tossOrderId("saga-insufficient-order-id")
                                .refundTotal(0L)
                                .build());

                seedUserDeposit(userUuid, 2000L);
                List<PaymentSuccessEvent.ItemDepositUsage> itemDepositUsages = List.of(
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 3000L),
                                new PaymentSuccessEvent.ItemDepositUsage(UUID.randomUUID(), 5000L));
                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(Map.of("statusCode", 200));

                // when: 예치금 차감을 실행한다
                deductDepositForPaymentUseCase.execute(
                                userUuid,
                                8000L,
                                orderUuid,
                                savedPayment.getUuid(),
                                itemDepositUsages);

                // then: 사용자 차감 이력(USE)은 남지 않고 결제는 FAILED로 보상 처리된다
                Payment compensatedPayment = waitForPaymentStatus(savedPayment.getUuid(), PaymentStatus.FAILED,
                                Duration.ofSeconds(3));
                Deposit userDeposit = findDepositUseCase.findOrCreate(userUuid);
                List<DepositHistory> userHistories = depositSupport.findHistoriesByUserUuid(userUuid);

                assertThat(userDeposit.getBalance()).isEqualTo(2000L);
                assertThat(userHistories).hasSize(1);
                assertThat(userHistories.get(0).getType()).isEqualTo(DepositHistoryType.CHARGE);
                assertThat(compensatedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
                assertThat(paymentSupport.hasHistoryType(compensatedPayment.getId(), PaymentHistoryType.PAYMENT_FAILED))
                                .isTrue();
                verify(tossPaymentClient).cancel(eq("saga-insufficient-payment-key"),
                                argThat(body -> ((Number) body.get("cancelAmount")).longValue() == 5000L));
        }

        @Test
        @DisplayName("이벤트 체인 성공: 결제 성공 이벤트가 주문 PAID와 예치금 차감 완료로 이어진다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void completeEventChainWhenPaymentSuccessEventAndDepositDeductionSuccess() {
                // given: 주문(PENDING), 결제(DONE), 사용자 예치금(차감 가능) 상태를 준비한다
                UUID userUuid = UUID.randomUUID();

                Order order = createPendingOrder(userUuid);
                UUID orderItemUuid1 = order.getOrderItems().get(0).getUuid();
                UUID orderItemUuid2 = order.getOrderItems().get(1).getUuid();

                Payment payment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(order.getUuid())
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("event-chain-success-payment-key")
                                .tossOrderId("event-chain-success-order-id")
                                .refundTotal(0L)
                                .build());

                seedUserDeposit(userUuid, 10000L);
                long userBefore = findDepositUseCase.findOrCreate(userUuid).getBalance();

                // when: 결제 성공 이벤트를 발행한다 (Order/Deposit 리스너 동시 트리거)
                publishPaymentSuccessEventAfterCommit(new PaymentSuccessEvent(
                                payment.getUuid(),
                                payment.getUuid(),
                                order.getUuid(),
                                payment.getAmount(),
                                8000L,
                                userUuid,
                                java.time.LocalDateTime.now(),
                                List.of(
                                                new PaymentSuccessEvent.ItemDepositUsage(orderItemUuid1, 3000L),
                                                new PaymentSuccessEvent.ItemDepositUsage(orderItemUuid2, 5000L))));

                // then: 주문은 PAID, 아이템은 PAYMENT_COMPLETED, 예치금은 정확히 차감되고 보상은 발생하지 않는다
                Order paidOrder = waitForOrderStatus(order.getUuid(), OrderStatus.PAID, Duration.ofSeconds(3));
                Deposit userDeposit = findDepositUseCase.findOrCreate(userUuid);
                Payment latestPayment = paymentSupport.findPaymentByUuid(payment.getUuid());

                assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);
                assertThat(paidOrder.getOrderItems().stream().map(OrderItem::getStatus))
                                .allMatch(status -> status == OrderItemStatus.PAYMENT_COMPLETED);
                assertThat(userDeposit.getBalance()).isEqualTo(userBefore - 8000L);
                assertThat(latestPayment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
                assertThat(paymentSupport.hasHistoryType(latestPayment.getId(), PaymentHistoryType.PAYMENT_FAILED))
                                .isFalse();
                verify(increaseDepositUseCase, atLeast(1)).increase(
                                eq(SystemDepositInitData.SYSTEM_USER_UUID),
                                eq(8000L),
                                eq(DepositHistoryType.CHARGE),
                                eq(order.getUuid()));
                verify(increaseDepositUseCase, atLeast(1)).increase(
                                eq(SystemDepositInitData.SYSTEM_USER_UUID),
                                eq(5000L),
                                eq(DepositHistoryType.CHARGE),
                                eq(order.getUuid()));
                verify(tossPaymentClient, never()).cancel(anyString(), anyMap());
        }

        @Test
        @DisplayName("이벤트 체인 실패: 예치금 차감 실패 시 결제 보상과 주문 상태 복원이 함께 검증된다")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void rollbackEventChainWhenPaymentSuccessEventAndDepositDeductionFail() {
                // given: 주문(PENDING), 결제(DONE), 사용자 예치금(차감 불가) 상태를 준비한다
                UUID userUuid = UUID.randomUUID();
                Order order = createPendingOrder(userUuid);
                UUID orderItemUuid1 = order.getOrderItems().get(0).getUuid();
                UUID orderItemUuid2 = order.getOrderItems().get(1).getUuid();

                Payment payment = paymentSupport.savePayment(Payment.builder()
                                .userUuid(userUuid)
                                .orderUuid(order.getUuid())
                                .amount(20000L)
                                .amountPg(5000L)
                                .amountPgOrigin(5000L)
                                .paymentDeposit(15000L)
                                .paymentDepositOrigin(15000L)
                                .paymentCouponTotal(0L)
                                .paymentType(PaymentType.MIXED)
                                .paymentStatus(PaymentStatus.DONE)
                                .pgPaymentKey("event-chain-fail-payment-key")
                                .tossOrderId("event-chain-fail-order-id")
                                .refundTotal(0L)
                                .build());
                seedUserDeposit(userUuid, 2000L);
                when(tossPaymentClient.cancel(anyString(), anyMap())).thenReturn(Map.of("statusCode", 200));

                // when: 결제 성공 이벤트를 발행한다 (예치금 차감은 첫 단계에서 실패)
                publishPaymentSuccessEventAfterCommit(new PaymentSuccessEvent(
                                payment.getUuid(),
                                payment.getUuid(),
                                order.getUuid(),
                                payment.getAmount(),
                                8000L,
                                userUuid,
                                java.time.LocalDateTime.now(),
                                List.of(
                                                new PaymentSuccessEvent.ItemDepositUsage(orderItemUuid1, 3000L),
                                                new PaymentSuccessEvent.ItemDepositUsage(orderItemUuid2, 5000L))));

                // then: 결제는 FAILED로 보상되어야 하고, 주문 상태도 최종적으로 CANCELED로 복원되어야 한다

                // [SAGA BUG 확인 지점]
                // 이벤트 체인(Success -> Deduct Fail -> Compensate)이 중간에 끊어지는 현상
                // PaymentSuccessEvent 발행 후 DeductUseCase가 REQUIRES_NEW로 실행되지만,
                // 여기서 발행된 DepositDeductionFailedEvent를 PaymentEventListener가 수신하지 못함
                Payment failedPayment = waitForPaymentStatus(payment.getUuid(), PaymentStatus.FAILED,
                                Duration.ofSeconds(3));
                Order canceledOrder = waitForOrderStatus(order.getUuid(), OrderStatus.CANCELED, Duration.ofSeconds(3));
                Deposit userDeposit = findDepositUseCase.findOrCreate(userUuid);
                List<DepositHistory> userHistories = depositSupport.findHistoriesByUserUuid(userUuid);

                assertThat(failedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
                assertThat(paymentSupport.hasHistoryType(failedPayment.getId(), PaymentHistoryType.PAYMENT_FAILED))
                                .isTrue();
                assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
                assertThat(canceledOrder.getOrderItems().stream().map(OrderItem::getStatus))
                                .allMatch(status -> status == OrderItemStatus.CANCELED);
                assertThat(userDeposit.getBalance()).isEqualTo(2000L);
                assertThat(userHistories).hasSize(1);
                assertThat(userHistories.get(0).getType()).isEqualTo(DepositHistoryType.CHARGE);
                verify(tossPaymentClient).cancel(eq("event-chain-fail-payment-key"),
                                argThat(body -> ((Number) body.get("cancelAmount")).longValue() == 5000L));
        }

        private Order createPendingOrder(UUID userUuid) {
                Order order = Order.builder()
                                .userUuid(userUuid)
                                .totalAmount(20000)
                                .address("서울시 강남구")
                                .recipient("테스트수령인")
                                .contactNumber("010-0000-0000")
                                .refundedAmount(0)
                                .status(OrderStatus.PENDING)
                                .build();

                order.addOrderItem(OrderItem.builder()
                                .productUuid(UUID.randomUUID())
                                .sellerUuid(UUID.randomUUID())
                                .productName("상품-1")
                                .productPrice(10000)
                                .imageUrl("https://example.com/item-1")
                                .status(OrderItemStatus.CANCEL_REQUESTED)
                                .build());
                order.addOrderItem(OrderItem.builder()
                                .productUuid(UUID.randomUUID())
                                .sellerUuid(UUID.randomUUID())
                                .productName("상품-2")
                                .productPrice(10000)
                                .imageUrl("https://example.com/item-2")
                                .status(OrderItemStatus.CANCEL_REQUESTED)
                                .build());

                return orderSupport.save(order);
        }

        private void seedUserDeposit(UUID userUuid, long balance) {
                Deposit deposit = Deposit.builder()
                                .userUuid(userUuid)
                                .depositUuid(UUID.randomUUID())
                                .balance(balance)
                                .version(0)
                                .build();
                depositSupport.save(deposit);
                depositSupport.saveHistory(DepositHistory.create(
                                userUuid,
                                balance,
                                balance,
                                DepositHistoryType.CHARGE,
                                UUID.randomUUID()));
        }

        private void publishPaymentSuccessEventAfterCommit(PaymentSuccessEvent event) {
                TransactionTemplate template = new TransactionTemplate(transactionManager);
                template.executeWithoutResult(status -> eventPublisher.publish(event));
        }

        private Order waitForOrderStatus(UUID orderUuid, OrderStatus status, Duration timeout) {
                long deadline = System.currentTimeMillis() + timeout.toMillis();
                Order latest = orderSupport.findOrderByUuidWithItems(orderUuid);

                while (System.currentTimeMillis() <= deadline) {
                        latest = orderSupport.findOrderByUuidWithItems(orderUuid);
                        if (latest.getStatus() == status) {
                                return latest;
                        }

                        try {
                                Thread.sleep(50L);
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return latest;
                        }
                }

                return latest;
        }

        private Payment waitForPaymentStatus(UUID paymentUuid, PaymentStatus status, Duration timeout) {
                long deadline = System.currentTimeMillis() + timeout.toMillis();
                Payment latest = paymentSupport.findPaymentByUuid(paymentUuid);

                while (System.currentTimeMillis() <= deadline) {
                        latest = paymentSupport.findPaymentByUuid(paymentUuid);
                        if (latest.getPaymentStatus() == status) {
                                return latest;
                        }

                        try {
                                Thread.sleep(50L);
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return latest;
                        }
                }

                return latest;
        }

        @TestConfiguration
        @EnableAsync
        static class AsyncTestConfig {
        }
}
