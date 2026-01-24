package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.shared.payment.dto.PaymentRefundRequest;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.common.shared.payment.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 결제 실패(보상 트랜잭션용) UseCase
 *
 * <p>
 * 예치금 차감 실패 등 후속 프로세스 오류 시 이미 승인된 PG 결제를 취소 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensatePaymentUseCase {

    private final PaymentSupport support;
    private final RefundPaymentUseCase refundPaymentUseCase;

    /**
     * 보상 트랜잭션 실행 (결제 취소 및 예치금 복구)
     *
     * <p>
     * 예치금 차감 실패, 주문 재고 부족 등 후속 프로세스에서 예외 발생 시 호출
     * 
     * @param orderUuid 관련 주문 UUID
     * @param reason    취소 사유
     */
    @Transactional
    public void execute(UUID orderUuid, String reason) {
        // 1. 주문 UUID로 결제 정보 조회 (최근 승인된 것 우선)
        Payment payment = support.findPaymentsByOrderUuid(orderUuid).stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.DONE)
                .findFirst()
                .orElse(null);

        if (payment == null) {
            log.warn("[보상 트랜잭션 건너뜀] 취소할 완료된 결제가 없음: orderUuid={}", orderUuid);
            return;
        }

        log.info("[보상 트랜잭션 시작] 결제 취소 시도: paymentUuid={}, orderUuid={}, 사유={}",
                payment.getUuid(), orderUuid, reason);

        // 2. RefundPaymentUseCase를 활용하여 통합 환불 로직(PG 취소 + 예치금 복구) 실행
        try {
            PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
                    .paymentId(payment.getUuid())
                    .refundAmount(payment.getAmount()) // 보상 트랜잭션은 기본적으로 전액 취소
                    .reason("COMPENSATION: " + reason)
                    .build();

            // 멱등성 키로 결제 UUID 활용 (동일 주문에 대한 중복 롤백 방지)
            refundPaymentUseCase.execute(refundRequest, "COMP-" + payment.getUuid());

            log.info("[보상 트랜잭션 완료] 취소 및 복구 성공: paymentUuid={}", payment.getUuid());
        } catch (Exception e) {
            // 보상 트랜잭션은 최후의 복구 수단이므로, 예상치 못한 모든 예외(NPE 등)를 잡아서
            // 관리자에게 CRITICAL 로그로 알리고 상위로 전파해야 함.
            // 이때 발생한 exception은 콜스택을 타고 상위 호출자에게 전파됨.
            // 예외의 세부 종류에 대해서는 일단 나누지 않음. (실패 사실이 더 중요함)
            log.error("[CRITICAL][보상 트랜잭션 최종 실패] 환불 로직 실행 중 예외 발생: {}. 관리자 점검 필요! paymentUuid={}",
                    e.getMessage(), payment.getUuid());
            // RefundPaymentUseCase 내부에서 이미 ROLLBACK_FAILED 상태 전이 및 이력 생성을 수행하므로 여기서는 로그만 남김
            throw e;
        }
    }
}
