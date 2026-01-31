package dukku.semicolon.boundedContext.deposit.app;

import dukku.common.global.eventPublisher.EventPublisher;
import dukku.common.shared.payment.event.PaymentSuccessEvent;
import dukku.semicolon.boundedContext.deposit.entity.enums.DepositHistoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * DepositFacade 단위 테스트
 */
class DepositFacadeTest {

    private DecreaseDepositUseCase decreaseDepositUseCase;
    private EventPublisher eventPublisher;
    private DepositFacade depositFacade;

    @BeforeEach
    void setUp() {
        decreaseDepositUseCase = mock(DecreaseDepositUseCase.class);
        eventPublisher = mock(EventPublisher.class);
        // 테스트와 직접적 연관이 없는 의존성은 mock으로 주입
        depositFacade = new DepositFacade(
                mock(FindDepositUseCase.class),
                mock(IncreaseDepositUseCase.class),
                decreaseDepositUseCase,
                mock(FindDepositHistoriesUseCase.class),
                eventPublisher);
    }

    @Test
    @DisplayName("예치금 차감 테스트: 아이템별 사용 내역에 따라 각각 차감 로직이 호출되어야 한다")
    void deductDepositForPaymentTest() {
        // Given: 유저 정보 및 상품별 예치금 사용 상세 내역 준비
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        UUID itemUuid1 = UUID.randomUUID();
        UUID itemUuid2 = UUID.randomUUID();

        List<PaymentSuccessEvent.ItemDepositUsage> usages = Arrays.asList(
                new PaymentSuccessEvent.ItemDepositUsage(itemUuid1, 5000L),
                new PaymentSuccessEvent.ItemDepositUsage(itemUuid2, 3000L));

        // When: 예치금 차감 프로세스 실행
        depositFacade.deductDepositForPayment(userUuid, 8000L, orderUuid, usages);

        // Then: DecreaseDepositUseCase가 각 상품별 금액에 대해 올바른 타입(USE)과 아이템 UUID로 호출되었는지 검증
        verify(decreaseDepositUseCase).decrease(userUuid, 5000L, DepositHistoryType.USE, itemUuid1);
        verify(decreaseDepositUseCase).decrease(userUuid, 3000L, DepositHistoryType.USE, itemUuid2);

        // 최종적으로 차감 완료 이벤트가 발행되었는지 확인
        verify(eventPublisher).publish(any());
    }
}
