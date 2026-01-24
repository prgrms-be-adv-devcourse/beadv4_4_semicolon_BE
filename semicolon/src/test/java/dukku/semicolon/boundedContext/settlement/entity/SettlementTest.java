package dukku.semicolon.boundedContext.settlement.entity;

import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Settlement 엔티티 단위 테스트
 * - 정산 생성 로직 검증
 * - 상태 변경 로직 검증
 * - 수수료 계산 검증
 */
class SettlementTest {

    @Test
    @DisplayName("정산 생성 - 정상 생성")
    void testCreate_Success() {
        // Given
        UUID sellerUuid = UUID.randomUUID();
        UUID buyerUuid = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        UUID depositId = UUID.randomUUID();
        Long totalAmount = 100000L;
        BigDecimal feeRate = new BigDecimal("0.05");
        LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

        // When
        Settlement settlement = Settlement.create(
                sellerUuid, buyerUuid, paymentId, orderId, orderItemId,
                depositId, totalAmount, feeRate, reservationDate
        );

        // Then
        assertThat(settlement).isNotNull();
        assertThat(settlement.getSellerUuid()).isEqualTo(sellerUuid);
        assertThat(settlement.getBuyerUuid()).isEqualTo(buyerUuid);
        assertThat(settlement.getPaymentId()).isEqualTo(paymentId);
        assertThat(settlement.getOrderId()).isEqualTo(orderId);
        assertThat(settlement.getOrderItemId()).isEqualTo(orderItemId);
        assertThat(settlement.getDepositId()).isEqualTo(depositId);
        assertThat(settlement.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(settlement.getFee()).isEqualByComparingTo(feeRate);
        assertThat(settlement.getSettlementReservationDate()).isEqualTo(reservationDate);
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("정산 생성 - 수수료 5% 계산")
    void testCreate_FeeCalculation_5Percent() {
        // Given: 총액 100,000원, 수수료율 5%
        Long totalAmount = 100000L;
        BigDecimal feeRate = new BigDecimal("0.05");

        // When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                totalAmount, feeRate, LocalDateTime.now().plusDays(1)
        );

        // Then: 수수료 5,000원, 정산금액 95,000원
        assertThat(settlement.getFeeAmount()).isEqualTo(5000L);
        assertThat(settlement.getSettlementAmount()).isEqualTo(95000L);
    }

    @Test
    @DisplayName("정산 생성 - 수수료 3% 계산")
    void testCreate_FeeCalculation_3Percent() {
        // Given: 총액 50,000원, 수수료율 3%
        Long totalAmount = 50000L;
        BigDecimal feeRate = new BigDecimal("0.03");

        // When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                totalAmount, feeRate, LocalDateTime.now().plusDays(1)
        );

        // Then: 수수료 1,500원, 정산금액 48,500원
        assertThat(settlement.getFeeAmount()).isEqualTo(1500L);
        assertThat(settlement.getSettlementAmount()).isEqualTo(48500L);
    }

    @Test
    @DisplayName("정산 생성 - 초기 상태는 PENDING")
    void testCreate_InitialStatusIsPending() {
        // Given & When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );

        // Then
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(settlement.isPending()).isTrue();
        assertThat(settlement.isCompleted()).isFalse();
        assertThat(settlement.isFailed()).isFalse();
    }

    @Test
    @DisplayName("상태 변경 - PENDING → PROCESSING")
    void testStatusChange_PendingToProcessing() {
        // Given: PENDING 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );

        // When: PROCESSING으로 변경
        settlement.startProcessing();

        // Then
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PROCESSING);
    }

    @Test
    @DisplayName("상태 변경 - PROCESSING → SUCCESS")
    void testStatusChange_ProcessingToSuccess() {
        // Given: PROCESSING 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );
        settlement.startProcessing();

        // When: SUCCESS로 변경
        settlement.complete();

        // Then
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.SUCCESS);
        assertThat(settlement.isCompleted()).isTrue();
        assertThat(settlement.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("상태 변경 - PROCESSING → FAILED")
    void testStatusChange_ProcessingToFailed() {
        // Given: PROCESSING 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );
        settlement.startProcessing();

        // When: FAILED로 변경
        settlement.fail();

        // Then
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(settlement.isFailed()).isTrue();
    }

    @Test
    @DisplayName("상태 변경 - FAILED → PENDING (재시도)")
    void testStatusChange_FailedToPending_Retry() {
        // Given: FAILED 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );
        settlement.startProcessing();
        settlement.fail();

        // When: PENDING으로 재시도
        settlement.retry();

        // Then
        assertThat(settlement.getSettlementStatus()).isEqualTo(SettlementStatus.PENDING);
        assertThat(settlement.isPending()).isTrue();
    }

    @Test
    @DisplayName("상태 변경 실패 - 잘못된 상태 전이")
    void testStatusChange_InvalidTransition() {
        // Given: PENDING 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );

        // When & Then: PENDING → SUCCESS는 불가능 (PROCESSING을 거쳐야 함)
        assertThatThrownBy(settlement::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("정산 상태를")
                .hasMessageContaining("변경할 수 없습니다");
    }

    @Test
    @DisplayName("상태 변경 실패 - SUCCESS 상태에서는 변경 불가")
    void testStatusChange_SuccessIsTerminal() {
        // Given: SUCCESS 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );
        settlement.startProcessing();
        settlement.complete();

        // When & Then: SUCCESS에서 다른 상태로 변경 불가
        assertThatThrownBy(settlement::fail)
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(settlement::retry)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("수수료 계산 - 소수점 처리")
    void testFeeCalculation_DecimalHandling() {
        // Given: 수수료율 5%일 때 총액이 홀수인 경우
        Long totalAmount = 10001L;  // 10,001원
        BigDecimal feeRate = new BigDecimal("0.05");

        // When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                totalAmount, feeRate, LocalDateTime.now().plusDays(1)
        );

        // Then: 수수료 500원 (10,001 * 0.05 = 500.05 → 500), 정산금액 9,501원
        assertThat(settlement.getFeeAmount()).isEqualTo(500L);
        assertThat(settlement.getSettlementAmount()).isEqualTo(9501L);
        assertThat(settlement.getTotalAmount()).isEqualTo(settlement.getFeeAmount() + settlement.getSettlementAmount());
    }

    @Test
    @DisplayName("수수료 계산 - 총액과 정산금액+수수료 일치")
    void testFeeCalculation_TotalAmountConsistency() {
        // Given
        Long totalAmount = 123456L;
        BigDecimal feeRate = new BigDecimal("0.05");

        // When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                totalAmount, feeRate, LocalDateTime.now().plusDays(1)
        );

        // Then: 총액 = 수수료 + 정산금액
        assertThat(settlement.getTotalAmount())
                .isEqualTo(settlement.getFeeAmount() + settlement.getSettlementAmount());
    }

    @Test
    @DisplayName("정산 예약일 설정 확인")
    void testReservationDate() {
        // Given
        LocalDateTime reservationDate = LocalDateTime.of(2026, 1, 25, 0, 0, 0);

        // When
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), reservationDate
        );

        // Then
        assertThat(settlement.getSettlementReservationDate()).isEqualTo(reservationDate);
    }

    @Test
    @DisplayName("완료 시간 설정 - complete 호출 시에만 설정됨")
    void testCompletedAt_OnlySetWhenComplete() {
        // Given: PROCESSING 상태의 정산
        Settlement settlement = Settlement.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                100000L, new BigDecimal("0.05"), LocalDateTime.now().plusDays(1)
        );
        settlement.startProcessing();

        // When: complete 호출 전에는 null
        assertThat(settlement.getCompletedAt()).isNull();

        // complete 호출
        settlement.complete();

        // Then: complete 호출 후에는 시간이 설정됨
        assertThat(settlement.getCompletedAt()).isNotNull();
        assertThat(settlement.getCompletedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }
}
