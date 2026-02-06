package dukku.semicolon.global;

import dukku.semicolon.boundedContext.settlement.entity.Settlement;
import dukku.semicolon.boundedContext.settlement.entity.type.SettlementStatus;
import dukku.semicolon.boundedContext.settlement.out.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Settlement 테스트용 초기 데이터 생성
 * - dev, test 프로파일에서만 동작
 * - 다양한 상태(PENDING, PROCESSING, SUCCESS, FAILED)의 정산 데이터 생성
 * - Swagger 테스트를 위한 고정된 UUID 사용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(4)
@Profile({"dev", "test"})
public class SettlementInitData {

    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.05");

    // ===== 고정 UUID (Swagger 테스트용) =====
    // 판매자 UUID (5명)
    private static final UUID SELLER_1 = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SELLER_2 = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f2345678901a");
    private static final UUID SELLER_3 = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-234567890abc");
    private static final UUID SELLER_4 = UUID.fromString("d4e5f6a7-b8c9-0123-def0-34567890abcd");
    private static final UUID SELLER_5 = UUID.fromString("e5f6a7b8-c9d0-1234-ef01-4567890abcde");

    // 구매자 UUID (10명)
    private static final UUID BUYER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BUYER_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID BUYER_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID BUYER_4 = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID BUYER_5 = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID BUYER_6 = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID BUYER_7 = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID BUYER_8 = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID BUYER_9 = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID BUYER_10 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    // 예치금 계좌 UUID
    private static final UUID DEPOSIT_1 = UUID.fromString("d0000001-0000-0000-0000-000000000001");
    private static final UUID DEPOSIT_2 = UUID.fromString("d0000002-0000-0000-0000-000000000002");
    private static final UUID DEPOSIT_3 = UUID.fromString("d0000003-0000-0000-0000-000000000003");
    private static final UUID DEPOSIT_4 = UUID.fromString("d0000004-0000-0000-0000-000000000004");
    private static final UUID DEPOSIT_5 = UUID.fromString("d0000005-0000-0000-0000-000000000005");

    // Swagger 테스트용 정산 UUID (상태별 대표 데이터)
    public static final UUID SETTLEMENT_SUCCESS_SAMPLE = UUID.fromString("f1e2d3c4-b5a6-7890-cdef-1234567890ab");
    public static final UUID SETTLEMENT_PROCESSING_SAMPLE = UUID.fromString("e2d3c4b5-a6f7-8901-bcde-234567890abc");
    public static final UUID SETTLEMENT_PENDING_SAMPLE = UUID.fromString("d3c4b5a6-f789-0123-cdef-34567890abcd");
    public static final UUID SETTLEMENT_FAILED_SAMPLE = UUID.fromString("c4b5a6f7-8901-2345-def0-4567890abcde");

    @Bean
    public CommandLineRunner initSettlements(SettlementRepository settlementRepository) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                if (settlementRepository.count() > 0) {
                    log.info("✅ [SettlementInitData] 이미 데이터가 존재합니다. 초기화 건너뜀.");
                    return;
                }

                log.info("🚀 [SettlementInitData] 테스트 데이터 생성 시작...");

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime pastReservation = now.minusDays(1); // 과거 (처리 가능)
                LocalDateTime futureReservation = now.plusMonths(1).withDayOfMonth(1); // 미래 (다음 달 1일)

                // ===== PENDING 상태 (12건) =====
                log.info("📝 PENDING 상태 정산 생성 중...");

                // 과거 예약일 - 처리 대기 중 (8건)
                createSettlement(settlementRepository, SETTLEMENT_PENDING_SAMPLE, SELLER_1, BUYER_1, DEPOSIT_1, 100000L, pastReservation);
                createSettlement(settlementRepository, SELLER_1, BUYER_2, DEPOSIT_1, 250000L, pastReservation);
                createSettlement(settlementRepository, SELLER_2, BUYER_3, DEPOSIT_2, 50000L, pastReservation);
                createSettlement(settlementRepository, SELLER_3, BUYER_4, DEPOSIT_3, 180000L, pastReservation);
                createSettlement(settlementRepository, SELLER_4, BUYER_5, DEPOSIT_4, 320000L, pastReservation);
                createSettlement(settlementRepository, SELLER_5, BUYER_6, DEPOSIT_5, 75000L, pastReservation);
                createSettlement(settlementRepository, SELLER_1, BUYER_7, DEPOSIT_1, 420000L, pastReservation);
                createSettlement(settlementRepository, SELLER_2, BUYER_8, DEPOSIT_2, 95000L, pastReservation);

                // 미래 예약일 - 아직 정산 시점 미도래 (4건)
                createSettlement(settlementRepository, SELLER_3, BUYER_9, DEPOSIT_3, 280000L, futureReservation);
                createSettlement(settlementRepository, SELLER_4, BUYER_10, DEPOSIT_4, 150000L, futureReservation);
                createSettlement(settlementRepository, SELLER_5, BUYER_1, DEPOSIT_5, 65000L, futureReservation);
                createSettlement(settlementRepository, SELLER_1, BUYER_2, DEPOSIT_1, 540000L, futureReservation);

                // ===== PROCESSING 상태 (8건) =====
                log.info("⚙️ PROCESSING 상태 정산 생성 중...");

                Settlement proc1 = createSettlement(settlementRepository, SETTLEMENT_PROCESSING_SAMPLE, SELLER_1, BUYER_3, DEPOSIT_1, 120000L, pastReservation);
                proc1.startProcessing();

                Settlement proc2 = createSettlement(settlementRepository, SELLER_2, BUYER_4, DEPOSIT_2, 350000L, pastReservation);
                proc2.startProcessing();

                Settlement proc3 = createSettlement(settlementRepository, SELLER_3, BUYER_5, DEPOSIT_3, 88000L, pastReservation);
                proc3.startProcessing();

                Settlement proc4 = createSettlement(settlementRepository, SELLER_4, BUYER_6, DEPOSIT_4, 210000L, pastReservation);
                proc4.startProcessing();

                Settlement proc5 = createSettlement(settlementRepository, SELLER_5, BUYER_7, DEPOSIT_5, 470000L, pastReservation);
                proc5.startProcessing();

                Settlement proc6 = createSettlement(settlementRepository, SELLER_1, BUYER_8, DEPOSIT_1, 92000L, pastReservation);
                proc6.startProcessing();

                Settlement proc7 = createSettlement(settlementRepository, SELLER_2, BUYER_9, DEPOSIT_2, 185000L, pastReservation);
                proc7.startProcessing();

                Settlement proc8 = createSettlement(settlementRepository, SELLER_3, BUYER_10, DEPOSIT_3, 310000L, pastReservation);
                proc8.startProcessing();

                // ===== SUCCESS 상태 (20건) =====
                log.info("✅ SUCCESS 상태 정산 생성 중...");

                Settlement succ1 = createSettlement(settlementRepository, SETTLEMENT_SUCCESS_SAMPLE, SELLER_1, BUYER_4, DEPOSIT_1, 50000L, pastReservation.minusDays(5));
                succ1.startProcessing();
                succ1.complete();

                Settlement succ2 = createSettlement(settlementRepository, SELLER_1, BUYER_5, DEPOSIT_1, 120000L, pastReservation.minusDays(3));
                succ2.startProcessing();
                succ2.complete();

                Settlement succ3 = createSettlement(settlementRepository, SELLER_2, BUYER_6, DEPOSIT_2, 85000L, pastReservation.minusDays(7));
                succ3.startProcessing();
                succ3.complete();

                Settlement succ4 = createSettlement(settlementRepository, SELLER_3, BUYER_7, DEPOSIT_3, 450000L, pastReservation.minusDays(10));
                succ4.startProcessing();
                succ4.complete();

                Settlement succ5 = createSettlement(settlementRepository, SELLER_4, BUYER_8, DEPOSIT_4, 95000L, pastReservation.minusDays(2));
                succ5.startProcessing();
                succ5.complete();

                Settlement succ6 = createSettlement(settlementRepository, SELLER_5, BUYER_9, DEPOSIT_5, 380000L, pastReservation.minusDays(4));
                succ6.startProcessing();
                succ6.complete();

                Settlement succ7 = createSettlement(settlementRepository, SELLER_1, BUYER_10, DEPOSIT_1, 62000L, pastReservation.minusDays(6));
                succ7.startProcessing();
                succ7.complete();

                Settlement succ8 = createSettlement(settlementRepository, SELLER_2, BUYER_1, DEPOSIT_2, 520000L, pastReservation.minusDays(8));
                succ8.startProcessing();
                succ8.complete();

                Settlement succ9 = createSettlement(settlementRepository, SELLER_3, BUYER_2, DEPOSIT_3, 145000L, pastReservation.minusDays(9));
                succ9.startProcessing();
                succ9.complete();

                Settlement succ10 = createSettlement(settlementRepository, SELLER_4, BUYER_3, DEPOSIT_4, 275000L, pastReservation.minusDays(11));
                succ10.startProcessing();
                succ10.complete();

                Settlement succ11 = createSettlement(settlementRepository, SELLER_5, BUYER_4, DEPOSIT_5, 98000L, pastReservation.minusDays(12));
                succ11.startProcessing();
                succ11.complete();

                Settlement succ12 = createSettlement(settlementRepository, SELLER_1, BUYER_5, DEPOSIT_1, 410000L, pastReservation.minusDays(13));
                succ12.startProcessing();
                succ12.complete();

                Settlement succ13 = createSettlement(settlementRepository, SELLER_2, BUYER_6, DEPOSIT_2, 77000L, pastReservation.minusDays(14));
                succ13.startProcessing();
                succ13.complete();

                Settlement succ14 = createSettlement(settlementRepository, SELLER_3, BUYER_7, DEPOSIT_3, 195000L, pastReservation.minusDays(15));
                succ14.startProcessing();
                succ14.complete();

                Settlement succ15 = createSettlement(settlementRepository, SELLER_4, BUYER_8, DEPOSIT_4, 330000L, pastReservation.minusDays(16));
                succ15.startProcessing();
                succ15.complete();

                Settlement succ16 = createSettlement(settlementRepository, SELLER_5, BUYER_9, DEPOSIT_5, 115000L, pastReservation.minusDays(17));
                succ16.startProcessing();
                succ16.complete();

                Settlement succ17 = createSettlement(settlementRepository, SELLER_1, BUYER_10, DEPOSIT_1, 245000L, pastReservation.minusDays(18));
                succ17.startProcessing();
                succ17.complete();

                Settlement succ18 = createSettlement(settlementRepository, SELLER_2, BUYER_1, DEPOSIT_2, 480000L, pastReservation.minusDays(19));
                succ18.startProcessing();
                succ18.complete();

                Settlement succ19 = createSettlement(settlementRepository, SELLER_3, BUYER_2, DEPOSIT_3, 82000L, pastReservation.minusDays(20));
                succ19.startProcessing();
                succ19.complete();

                Settlement succ20 = createSettlement(settlementRepository, SELLER_4, BUYER_3, DEPOSIT_4, 360000L, pastReservation.minusDays(21));
                succ20.startProcessing();
                succ20.complete();

                // ===== FAILED 상태 (5건) =====
                log.info("❌ FAILED 상태 정산 생성 중...");

                Settlement fail1 = createSettlement(settlementRepository, SETTLEMENT_FAILED_SAMPLE, SELLER_1, BUYER_6, DEPOSIT_1, 300000L, pastReservation.minusDays(1));
                fail1.startProcessing();
                fail1.fail();

                Settlement fail2 = createSettlement(settlementRepository, SELLER_2, BUYER_7, DEPOSIT_2, 220000L, pastReservation.minusDays(1));
                fail2.startProcessing();
                fail2.fail();

                Settlement fail3 = createSettlement(settlementRepository, SELLER_3, BUYER_8, DEPOSIT_3, 155000L, pastReservation.minusDays(2));
                fail3.startProcessing();
                fail3.fail();

                Settlement fail4 = createSettlement(settlementRepository, SELLER_4, BUYER_9, DEPOSIT_4, 425000L, pastReservation.minusDays(2));
                fail4.startProcessing();
                fail4.fail();

                Settlement fail5 = createSettlement(settlementRepository, SELLER_5, BUYER_10, DEPOSIT_5, 68000L, pastReservation.minusDays(3));
                fail5.startProcessing();
                fail5.fail();

                long totalCount = settlementRepository.count();
                log.info("✅ [SettlementInitData] 테스트 데이터 생성 완료. 총 {}건", totalCount);
                log.info("  📊 상태별 분포:");
                log.info("    - PENDING: 12건 (과거 8건, 미래 4건)");
                log.info("    - PROCESSING: 8건");
                log.info("    - SUCCESS: 20건");
                log.info("    - FAILED: 5건");
                log.info("");
                log.info("  👥 판매자 정보:");
                log.info("    - SELLER_1 (홍길동상점): {}", SELLER_1);
                log.info("    - SELLER_2 (테크마스터): {}", SELLER_2);
                log.info("    - SELLER_3 (소리사랑): {}", SELLER_3);
                log.info("    - SELLER_4 (숲속의집): {}", SELLER_4);
                log.info("    - SELLER_5 (나이스샷): {}", SELLER_5);
                log.info("");
                log.info("  🔍 Swagger 테스트용 UUID:");
                log.info("    - SUCCESS 샘플: {}", SETTLEMENT_SUCCESS_SAMPLE);
                log.info("    - PROCESSING 샘플: {}", SETTLEMENT_PROCESSING_SAMPLE);
                log.info("    - PENDING 샘플: {}", SETTLEMENT_PENDING_SAMPLE);
                log.info("    - FAILED 샘플: {}", SETTLEMENT_FAILED_SAMPLE);
            }

            private Settlement createSettlement(
                    SettlementRepository settlementRepository,
                    UUID sellerUuid,
                    UUID buyerUuid,
                    UUID depositId,
                    Long totalAmount,
                    LocalDateTime reservationDate
            ) {
                Settlement settlement = Settlement.create(
                        sellerUuid,
                        buyerUuid,
                        UUID.randomUUID(), // paymentId
                        UUID.randomUUID(), // orderId
                        UUID.randomUUID(), // orderItemId
                        depositId,
                        totalAmount,
                        DEFAULT_FEE_RATE,
                        reservationDate
                );

                return settlementRepository.save(settlement);
            }

            private Settlement createSettlement(
                    SettlementRepository settlementRepository,
                    UUID settlementUuid,
                    UUID sellerUuid,
                    UUID buyerUuid,
                    UUID depositId,
                    Long totalAmount,
                    LocalDateTime reservationDate
            ) {
                // 수수료 계산
                long feeAmount = (long) (totalAmount * DEFAULT_FEE_RATE.doubleValue());
                long settlementAmount = totalAmount - feeAmount;

                // Builder를 사용하여 UUID 포함 모든 필드 설정
                Settlement settlement = Settlement.builder()
                        .uuid(settlementUuid)
                        .sellerUuid(sellerUuid)
                        .buyerUuid(buyerUuid)
                        .paymentId(UUID.randomUUID())
                        .orderId(UUID.fromString("b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11")) // orderUuid (Swagger 테스트용 고정)
                        .orderItemId(UUID.randomUUID())
                        .depositId(depositId)
                        .totalAmount(totalAmount)
                        .fee(DEFAULT_FEE_RATE)
                        .feeAmount(feeAmount)
                        .settlementAmount(settlementAmount)
                        .settlementStatus(SettlementStatus.PENDING)
                        .settlementReservationDate(reservationDate)
                        .build();

                return settlementRepository.save(settlement);
            }
        };
    }
}
