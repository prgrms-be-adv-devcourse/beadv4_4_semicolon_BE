package dukku.semicolon.boundedContext.payment.app;

import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.shared.payment.dto.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * 결제 내역 조회 UseCase
 *
 * <p>
 * 결제 UUID로 단건 조회하여 상세 정보 반환
 */
@Component
@RequiredArgsConstructor
public class FindPaymentUseCase {

    private final PaymentSupport support;

    @Transactional(readOnly = true)
    public Payment execute(UUID paymentUuid) {
        return support.findPaymentByUuid(paymentUuid);
    }
}
