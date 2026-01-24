package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import dukku.common.shared.payment.type.PaymentHistoryType;
import dukku.common.shared.payment.type.PaymentType;
import dukku.semicolon.shared.payment.dto.PaymentRequest;
import dukku.semicolon.shared.payment.dto.PaymentResponse;
import dukku.semicolon.shared.payment.exception.AmountMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 결제 요청(준비) UseCase
 *
 * <p>
 * 프론트에서 결제 준비 요청 시 토스 결제창 호출에 필요한 정보 반환
 * {@link Payment} 엔티티를 PENDING 상태로 생성하고 토스 orderId를 발급
 */
@Component
@RequiredArgsConstructor
public class RequestPaymentUseCase {

    private final PaymentSupport support;

    /**
     * 결제 요청 처리
     *
     * @param request        결제 요청 DTO
     * @param idempotencyKey 멱등성 키 (중복 요청 방지)
     * @return 토스 결제창 호출에 필요한 정보
     */
    public PaymentResponse execute(PaymentRequest request, String idempotencyKey) {
        // 1. 멱등성 검증 - 동일 주문에 대한 중복 결제 요청 방지
        Optional<PaymentResponse> cachedResponse = checkIdempotency(request.getOrderUuid(), idempotencyKey);
        if (cachedResponse.isPresent()) {
            return cachedResponse.get();
        }

        // 2. 금액 유효성 검증
        validateAmounts(request.getAmounts());

        // 3. Payment 생성 (PENDING 상태) 및 스냅샷 기록
        String tossOrderId = generateTossOrderId(UUID.randomUUID()); // 또는 로직 변경
        Payment payment = createPayment(request, tossOrderId);

        // 3-1. 결제 요청 이력 생성 (Support 위임)
        support.createHistory(payment, PaymentHistoryType.PAYMENT_REQUESTED, null, 0L, 0L);

        // 4. 응답 생성
        return payment.toPaymentResponse(request.getOrderName());
    }

    /**
     * 멱등성 검증 (중복 결제 요청 방지)
     *
     * @param orderUuid      주문 UUID
     * @param idempotencyKey 멱등성 키
     * @return 기존 결제가 있으면 해당 응답, 없으면 Empty
     */
    private Optional<PaymentResponse> checkIdempotency(UUID orderUuid, String idempotencyKey) {
        // TODO: Redis 또는 DB 기반 멱등성 검증 구현 (Phase 2)
        return Optional.empty();
    }

    /**
     * 금액 유효성 검증
     *
     * @param amounts 결제 요청 금액 정보
     * @throws AmountMismatchException 금액 계산이 일치하지 않을 때
     */
    private void validateAmounts(PaymentRequest.Amounts amounts) {
        // 1. 음수 금액 체크
        if (amounts.getFinalPayAmount() < 0
                || amounts.getDepositUseAmount() < 0
                || amounts.getPgPayAmount() < 0
                || amounts.getCouponDiscountAmount() < 0) {
            throw new AmountMismatchException("결제 금액은 0 이상이어야 합니다");
        }

        // 2. finalPayAmount = depositUseAmount + pgPayAmount 확인
        Long expectedFinalAmount = amounts.getDepositUseAmount() + amounts.getPgPayAmount();
        if (!expectedFinalAmount.equals(amounts.getFinalPayAmount())) {
            throw new AmountMismatchException(
                    "최종 결제금액 불일치: 예상=" + expectedFinalAmount
                            + " (예치금 " + amounts.getDepositUseAmount()
                            + " + PG " + amounts.getPgPayAmount() + ")"
                            + ", 실제=" + amounts.getFinalPayAmount());
        }

        // 3. itemsTotalAmount - couponDiscountAmount = finalPayAmount 확인
        Long expectedFromItems = amounts.getItemsTotalAmount() - amounts.getCouponDiscountAmount();
        if (!expectedFromItems.equals(amounts.getFinalPayAmount())) {
            throw new AmountMismatchException(
                    "상품금액 계산 불일치: 예상=" + expectedFromItems
                            + " (상품총액 " + amounts.getItemsTotalAmount()
                            + " - 쿠폰할인 " + amounts.getCouponDiscountAmount() + ")"
                            + ", 실제=" + amounts.getFinalPayAmount());
        }

        // 4. 예치금 잔액 검증
        // TODO: Deposit BC 연동 후 실제 잔액 조회 및 검증 로직 추가
    }

    /**
     * 결제 및 스냅샷 아이템 생성
     * 
     * <p>
     * 주문 정보를 기반으로 결제(Payment) 엔티티를 생성하고,
     * 총 예치금 사용액을 각 주문 상품(OrderItem)에 일관된 규칙으로 분배하여 기록한다.
     */
    private Payment createPayment(PaymentRequest request, String tossOrderId) {
        UUID userUuid = UserUtil.getUserId();
        PaymentRequest.Amounts amounts = request.getAmounts();

        // 결제 유형 결정 (전액 예치금 사용 vs PG 혼합 결제)
        PaymentType paymentType = amounts.getPgPayAmount() == 0
                ? PaymentType.DEPOSIT
                : PaymentType.MIXED;

        Payment payment = Payment.create(
                request.getOrderUuid(),
                userUuid,
                amounts.getFinalPayAmount(),
                amounts.getDepositUseAmount(),
                amounts.getPgPayAmount(),
                amounts.getCouponDiscountAmount(),
                paymentType,
                tossOrderId);

        // [상품별 예치금 분배 로직]
        // 1. productId ASC 정렬을 통해 배치 순서가 바뀌어도 항상 동일한 분배 결과 보장
        // 2. 각 상품별 결제 대상 금액(단가 - 쿠폰)을 한도로, 예치금이 소진될 때까지 순서대로 채움
        Long remainingDeposit = amounts.getDepositUseAmount();
        List<PaymentRequest.PaymentRequestItem> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(PaymentRequest.PaymentRequestItem::getProductId)
                        .thenComparing(PaymentRequest.PaymentRequestItem::getOrderItemUuid)) // 상품 ID가 같을 경우를 대비한 보조 정렬
                .toList();

        for (PaymentRequest.PaymentRequestItem itemDto : sortedItems) {
            // 이 상품에 최대로 적용 가능한 결제금액 계산
            Long itemPayableAmount = itemDto.getPrice() -
                    (itemDto.getPaymentCoupon() != null ? itemDto.getPaymentCoupon() : 0L);

            // 남은 예치금 중 이 상품에 할당할 금액 결정 (Min 연산)
            Long itemDepositUse = Math.min(remainingDeposit, itemPayableAmount);
            remainingDeposit -= itemDepositUse;

            // 결제 상품 스냅샷 생성 및 추가 (할당된 예치금 정보 포함)
            payment.addItem(PaymentOrderItem.create(
                    payment,
                    request.getOrderUuid(),
                    itemDto.getOrderItemUuid(),
                    itemDto.getProductId(),
                    itemDto.getProductName(),
                    itemDto.getPrice(),
                    itemDto.getPaymentCoupon(),
                    itemDto.getSellerUuid(),
                    itemDepositUse));
        }

        return support.savePayment(payment);
    }

    private String generateTossOrderId(UUID paymentUuid) {
        // 토스 orderId 형식: TOSS_{UUID 앞 8자}_{날짜}
        String datePart = LocalDateTime.now().toLocalDate().toString().replace("-", "");
        return "TOSS_" + paymentUuid.toString().substring(0, 8) + "_" + datePart;
    }
}
