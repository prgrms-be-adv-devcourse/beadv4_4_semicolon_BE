package dukku.semicolon.boundedContext.deposit.in;

import dukku.common.global.UserUtil;
import dukku.semicolon.shared.deposit.docs.DepositApiDocs;
import dukku.semicolon.shared.deposit.dto.DepositBalanceResponse;
import dukku.semicolon.shared.deposit.dto.DepositHistoryResponse;
import dukku.semicolon.boundedContext.deposit.app.DepositFacade;
import dukku.semicolon.shared.deposit.dto.DepositDto;
import dukku.semicolon.shared.deposit.dto.DepositHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 예치금 API 컨트롤러 (사용자용)
 *
 * <p>
 * 예치금 잔액 조회 및 변동 내역 조회 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
@DepositApiDocs.DepositTag
public class DepositController {

        private final DepositFacade depositFacade;

        /**
         * 내 예치금 잔액 조회
         *
         * <p>
         * 사용자의 현재 예치금 잔액을 조회한다.
         */
        @DepositApiDocs.GetMyBalance
        @GetMapping("/me/balance")
        public ResponseEntity<DepositBalanceResponse> getMyBalance() {
                UUID userUuid = UserUtil.getUserId();
                DepositDto depositDto = depositFacade.findDeposit(userUuid);

                DepositBalanceResponse response = DepositBalanceResponse.builder()
                                .success(true)
                                .code("DEPOSIT_BALANCE_RETRIEVED")
                                .message("예치금 잔액을 조회했습니다.")
                                .data(DepositBalanceResponse.DepositBalanceData.builder()
                                                .userUuid(depositDto.getUserUuid())
                                                .balance(depositDto.getBalance())
                                                .updatedAt(OffsetDateTime.of(depositDto.getUpdatedAt(),
                                                                java.time.ZoneOffset.of("+09:00"))) // KST 가정
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 내 예치금 변동 내역 조회
         *
         * <p>
         * 예치금의 충전, 사용, 환불 등 모든 변동 내역을 조회한다.
         */
        @DepositApiDocs.GetMyHistories
        @GetMapping("/me/histories")
        public ResponseEntity<DepositHistoryResponse> getMyHistories(
                        @RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) String cursor) {

                UUID userUuid = UserUtil.getUserId();
                // TODO: Pagination 구현 필요. 현재는 전체 조회 후 dummy slicing or just returning all for
                // MVP as per Facade implementation
                // For MVP, returning list as is. Pagination logic should be refined in
                // Facade/Repository if needed.
                List<DepositHistoryDto> histories = depositFacade.findHistories(userUuid);

                List<DepositHistoryResponse.DepositHistoryHistoryItem> items = histories.stream()
                                .map(this::toHistoryItem)
                                .toList();

                DepositHistoryResponse response = DepositHistoryResponse.builder()
                                .success(true)
                                .code("DEPOSIT_HISTORIES_RETRIEVED")
                                .message("예치금 내역을 조회했습니다.")
                                .data(DepositHistoryResponse.DepositHistoryData.builder()
                                                .items(items)
                                                .page(DepositHistoryResponse.PageInfo.builder()
                                                                .size(size)
                                                                .nextCursor(null) // TODO: Implement cursor pagination
                                                                .build())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        private DepositHistoryResponse.DepositHistoryHistoryItem toHistoryItem(DepositHistoryDto dto) {
                return DepositHistoryResponse.DepositHistoryHistoryItem.builder()
                                .depositHistoryId(String.valueOf(dto.getId()))
                                .type(dto.getType().name())
                                .amount(dto.getAmount()) // 절댓값 그대로 반환 (프론트에서 Type 보고 처리)
                                .balanceAfter(dto.getBalanceSnapshot())
                                .ref(DepositHistoryResponse.ReferenceInfo.builder()
                                                // TODO: OrderItem 등 연관 관계 조회 로직 추가 필요 시 구현
                                                .orderUuid(dto.getOrderItemUuid())
                                                .build())
                                .createdAt(OffsetDateTime.of(dto.getCreatedAt(), java.time.ZoneOffset.of("+09:00")))
                                .build();
        }
}
