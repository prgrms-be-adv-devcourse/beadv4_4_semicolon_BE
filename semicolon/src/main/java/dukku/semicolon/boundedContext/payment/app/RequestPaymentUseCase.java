package dukku.semicolon.boundedContext.payment.app;

import dukku.common.global.UserUtil;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrderItem;
import dukku.common.shared.payment.type.PaymentType;
import dukku.semicolon.shared.payment.dto.PaymentRequest;
import dukku.semicolon.shared.payment.dto.PaymentResponse;
import dukku.semicolon.shared.payment.exception.AmountMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
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
        // TODO: Redis 또는 DB 기반 멱등성 검증 구현
        // orderUUID로 존재유무 확인한다음 존재하면 생성안하는 방식으로 인메모리 대응할 수도 있을 거 같은데
        // 일단 나중에 생각하고 무조건 띄워주는 방식으로 구현하고 갈아끼우자
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

    private Payment createPayment(PaymentRequest request, String tossOrderId) {
        UUID userUuid = UserUtil.getUserId();
        PaymentRequest.Amounts amounts = request.getAmounts();

        // 결제 유형 결정 (예치금만 사용 vs 혼합)
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

        // 스냅샷 아이템 추가
        request.getItems().forEach(itemDto -> {
            payment.addItem(PaymentOrderItem.create(
                    payment,
                    request.getOrderUuid(),
                    itemDto.getOrderItemUuid(),
                    itemDto.getProductId(),
                    itemDto.getProductName(),
                    itemDto.getPrice(),
                    itemDto.getPaymentCoupon(),
                    itemDto.getSellerUuid()));
        });

        return support.savePayment(payment);
    }

    private String generateTossOrderId(UUID paymentUuid) {
        // 토스 orderId 형식: TOSS_{UUID 앞 8자}_{날짜}
        // TODO: 향후 한 주문에 대해 여러 번 결제 시도 시의 고유성 보장이 필요한 경우 정책에 따라 수정
        String datePart = LocalDateTime.now().toLocalDate().toString().replace("-", "");
        return "TOSS_" + paymentUuid.toString().substring(0, 8) + "_" + datePart;
    }

    }
}
