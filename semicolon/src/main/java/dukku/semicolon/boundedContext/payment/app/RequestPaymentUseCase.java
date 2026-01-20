package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.entity.PaymentOrder;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentOrderStatus;
import dukku.semicolon.boundedContext.payment.entity.enums.PaymentType;
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
 *
 * <p>
 * TODO: Order BC 머지 후 실제 주문 정보 연동 필요
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
     * @param userUuid       요청 사용자 UUID
     * @return 토스 결제창 호출에 필요한 정보
     */
    public PaymentResponse execute(PaymentRequest request, String idempotencyKey, UUID userUuid) {
        // 1. 멱등성 검증 - 동일 주문에 대한 중복 결제 요청 방지
        Optional<PaymentResponse> cachedResponse = checkIdempotency(request.getOrderUuid(), idempotencyKey);
        if (cachedResponse.isPresent()) {
            return cachedResponse.get();
        }

        // 2. 금액 유효성 검증
        validateAmounts(request.getAmounts());

        // 3. PaymentOrder 레플리카 동기화 (Order BC 미구현으로 더미 생성)
        PaymentOrder paymentOrder = syncPaymentOrderReplica(request.getOrderUuid(), userUuid);

        // 4. Payment 생성 (PENDING 상태)
        Payment payment = createPayment(paymentOrder, request, userUuid);

        // 5. 토스 orderId 생성
        String tossOrderId = generateTossOrderId(payment.getUuid());

        // 6. 응답 생성
        return buildResponse(payment, request, tossOrderId);
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
        // 검증에 사용할 Order 도메인의 구현이 아직 dev에 머지되지 않았음
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
        Integer expectedFinalAmount = amounts.getDepositUseAmount() + amounts.getPgPayAmount();
        if (!expectedFinalAmount.equals(amounts.getFinalPayAmount())) {
            throw new AmountMismatchException(
                    "최종 결제금액 불일치: 예상=" + expectedFinalAmount
                            + " (예치금 " + amounts.getDepositUseAmount()
                            + " + PG " + amounts.getPgPayAmount() + ")"
                            + ", 실제=" + amounts.getFinalPayAmount());
        }

        // 3. itemsTotalAmount - couponDiscountAmount = finalPayAmount 확인
        Integer expectedFromItems = amounts.getItemsTotalAmount() - amounts.getCouponDiscountAmount();
        if (!expectedFromItems.equals(amounts.getFinalPayAmount())) {
            throw new AmountMismatchException(
                    "상품금액 계산 불일치: 예상=" + expectedFromItems
                            + " (상품총액 " + amounts.getItemsTotalAmount()
                            + " - 쿠폰할인 " + amounts.getCouponDiscountAmount() + ")"
                            + ", 실제=" + amounts.getFinalPayAmount());
        }
    }

    /**
     * PaymentOrder 레플리카 동기화
     *
     * <p>
     * PaymentOrder는 Order BC의 레플리카로, 실제로는 Order BC에서 발행하는
     * OrderCreatedEvent를 수신하여 동기화해야 함
     * 현재는 Order BC가 미구현이므로 더미 데이터로 생성
     *
     * @param orderUuid 주문 UUID
     * @param userUuid  사용자 UUID
     * @return 동기화된 PaymentOrder
     */
    private PaymentOrder syncPaymentOrderReplica(UUID orderUuid, UUID userUuid) {
        // TODO: Order BC 머지 후 EventListener 기반 동기화로 변경
        // 실제 구현: OrderCreatedEvent 수신 → PaymentOrder 레플리카 저장
        // 결제 요청 시점에는 이미 존재하는 레플리카를 조회만 해야 함
        // 이 메소드는 결제 유스케이스 내부 동작을 위한 더미 데이터 생성을 하는 메소드임
        PaymentOrder paymentOrder = PaymentOrder.builder()
                .userUuid(userUuid)
                .status(PaymentOrderStatus.PAID)
                .build();

        return support.savePaymentOrder(paymentOrder);
    }

    private Payment createPayment(PaymentOrder paymentOrder, PaymentRequest request, UUID userUuid) {
        PaymentRequest.Amounts amounts = request.getAmounts();

        // 결제 유형 결정 (예치금만 사용 vs 혼합)
        PaymentType paymentType = amounts.getPgPayAmount() == 0
                ? PaymentType.DEPOSIT
                : PaymentType.MIXED;

        Payment payment = Payment.create(
                paymentOrder,
                userUuid,
                amounts.getFinalPayAmount(),
                amounts.getDepositUseAmount(),
                amounts.getPgPayAmount(),
                amounts.getCouponDiscountAmount(),
                paymentType);

        return support.savePayment(payment);
    }

    private String generateTossOrderId(UUID paymentUuid) {
        // TODO: Order BC 머지 후 Order UUID를 그대로 orderId로 사용
        // 현재는 임시로 Payment UUID 기반으로 생성
        // 토스 orderId 형식: TOSS_{UUID 앞 8자}_{날짜}
        String datePart = OffsetDateTime.now().toLocalDate().toString().replace("-", "");
        return "TOSS_" + paymentUuid.toString().substring(0, 8) + "_" + datePart;
    }

    private PaymentResponse buildResponse(Payment payment, PaymentRequest request, String tossOrderId) {
        PaymentRequest.Amounts requestAmounts = request.getAmounts();

        return PaymentResponse.builder()
                .paymentUuid(payment.getUuid())
                .status(payment.getPaymentStatus())
                .toss(PaymentResponse.TossInfo.builder()
                        .orderId(tossOrderId)
                        .amount(requestAmounts.getPgPayAmount())
                        .orderName(request.getOrderName())
                        // TODO: 실제 successUrl, failUrl은 설정에서 가져오기
                        .successUrl("https://localhost:3000/payments/success?paymentUuid=" + payment.getUuid())
                        .failUrl("https://localhost:3000/payments/fail?paymentUuid=" + payment.getUuid())
                        .build())
                .amounts(PaymentResponse.ResponseAmounts.builder()
                        .finalPayAmount(requestAmounts.getFinalPayAmount())
                        .depositUseAmount(requestAmounts.getDepositUseAmount())
                        .pgPayAmount(requestAmounts.getPgPayAmount())
                        .build())
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
