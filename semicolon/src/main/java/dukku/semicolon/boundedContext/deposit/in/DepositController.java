package dukku.semicolon.boundedContext.deposit.in;

import dukku.common.global.UserUtil;
import dukku.semicolon.shared.deposit.docs.DepositApiDocs;
import dukku.semicolon.shared.deposit.dto.DepositBalanceResponse;
import dukku.semicolon.shared.deposit.dto.DepositHistoryResponse;
import dukku.semicolon.boundedContext.deposit.app.DepositFacade;
import dukku.semicolon.shared.deposit.dto.DepositDto;
import dukku.semicolon.shared.deposit.dto.DepositHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
                                                .depositUuid(depositDto.getDepositUuid())
                                                .balance(depositDto.getBalance())
                                                .updatedAt(depositDto.getUpdatedAt() != null
                                                                ? depositDto.getUpdatedAt()
                                                                                .atOffset(ZoneOffset.ofHours(9))
                                                                : depositDto.getCreatedAt()
                                                                                .atOffset(ZoneOffset.ofHours(9)))
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
                        @RequestParam(defaultValue = "10") Integer size,
                        @RequestParam(required = false) String cursor) {

                UUID userUuid = UserUtil.getUserId();
                Integer cursorId = (cursor != null && !cursor.isBlank()) ? Integer.parseInt(cursor) : null;

                Slice<DepositHistoryDto> historySlice = depositFacade.findHistories(userUuid, cursorId, size);
                List<DepositHistoryDto> histories = historySlice.getContent();

                DepositHistoryResponse response = DepositHistoryResponse.builder()
                                .success(true)
                                .code("DEPOSIT_HISTORIES_RETRIEVED")
                                .message("예치금 내역을 조회했습니다.")
                                .data(DepositHistoryResponse.DepositHistoryData.builder()
                                                .items(histories.stream()
                                                                .map(this::mapToHistoryItem)
                                                                .toList())
                                                .page(DepositHistoryResponse.PageInfo.builder()
                                                                .size(size)
                                                                .nextCursor(historySlice.hasNext()
                                                                                ? histories.get(histories.size() - 1)
                                                                                                .getId().toString()
                                                                                : null)
                                                                .build())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        private DepositHistoryResponse.DepositHistoryHistoryItem mapToHistoryItem(DepositHistoryDto dto) {
                return DepositHistoryResponse.DepositHistoryHistoryItem.builder()
                                .depositHistoryId(dto.getId().toString())
                                .type(dto.getType().name())
                                .amount(dto.getAmount())
                                .balanceAfter(dto.getBalanceSnapshot())
                                .ref(DepositHistoryResponse.ReferenceInfo.builder()
                                                .orderUuid(dto.getOrderItemUuid())
                                                .build())
                                .createdAt(dto.getCreatedAt().atOffset(ZoneOffset.ofHours(9)))
                                .build();
        }
}
