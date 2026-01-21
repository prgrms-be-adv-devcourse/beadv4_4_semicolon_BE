package dukku.semicolon.boundedContext.deposit.in;

import dukku.semicolon.boundedContext.deposit.app.DepositFacade;
import dukku.semicolon.shared.deposit.docs.DepositApiDocs;
import dukku.semicolon.shared.deposit.docs.DepositApiDocs;
import dukku.semicolon.shared.deposit.dto.DepositAccountResponse;
import dukku.semicolon.shared.deposit.dto.DepositBalanceResponse;
import dukku.semicolon.shared.deposit.dto.DepositDto;
import dukku.semicolon.shared.deposit.dto.DepositHistoryDto;
import dukku.semicolon.shared.deposit.dto.DepositHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 관리자용 예치금 API 컨트롤러
 *
 * <p>
 * 관리자 전역 예치금 관리 및 특정 사용자 조회 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/v1/admin/deposits")
@RequiredArgsConstructor
@DepositApiDocs.DepositTag
public class AdminDepositController {

        private final DepositFacade depositFacade;

        /**
         * 특정 사용자 예치금 잔액 조회 (관리자용)
         *
         * <p>
         * 관리자가 특정 사용자의 현재 예치금 잔액을 조회한다.
         */
        @DepositApiDocs.GetUserBalance
        @GetMapping("/{userUuid}/balance")
        public ResponseEntity<DepositBalanceResponse> getUserBalance(@PathVariable UUID userUuid) {

                DepositDto deposit = depositFacade.findDeposit(userUuid);

                DepositBalanceResponse response = DepositBalanceResponse.builder().success(true)
                                .code("DEPOSIT_BALANCE_RETRIEVED").message("예치금 잔액을 조회했습니다.")
                                .data(DepositBalanceResponse.DepositBalanceData.builder()
                                                .userUuid(deposit.getUserUuid()).depositUuid(deposit.getDepositUuid())
                                                .balance(deposit.getBalance())
                                                .updatedAt(deposit.getUpdatedAt()
                                                                .atOffset(java.time.ZoneOffset.ofHours(9))) // KST
                                                                                                            // 처리
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 특정 사용자 예치금 계좌 조회 (관리자용)
         *
         * <p>
         * 특정 사용자의 예치금 계좌 UUID를 조회한다.
         */
        @GetMapping("/{userUuid}/account")
        public ResponseEntity<DepositAccountResponse> getUserAccount(@PathVariable UUID userUuid) {

                DepositDto deposit = depositFacade.findDeposit(userUuid);

                DepositAccountResponse response = DepositAccountResponse.builder()
                                .success(true)
                                .code("DEPOSIT_ACCOUNT_RETRIEVED")
                                .message("예치금 계좌 정보를 조회했습니다.")
                                .data(DepositAccountResponse.DepositAccountData.builder()
                                                .depositUuid(deposit.getDepositUuid())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 특정 사용자 예치금 내역 조회 (관리자용)
         *
         * <p>
         * 관리자가 특정 사용자의 예치금 변동 내역을 조회한다.
         */
        @DepositApiDocs.GetUserHistories
        @GetMapping("/{userUuid}/histories")
        public ResponseEntity<DepositHistoryResponse> getUserHistories(@PathVariable UUID userUuid,
                        @RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) String cursor) {

                List<DepositHistoryDto> histories = depositFacade.findHistories(userUuid);

                // TODO: Pagination Logic (현재는 전체 반환)
                List<DepositHistoryResponse.DepositHistoryHistoryItem> items = histories.stream()
                                .map(history -> DepositHistoryResponse.DepositHistoryHistoryItem.builder()
                                                .depositHistoryId(String.valueOf(history.getId()))
                                                .type(history.getType().name())
                                                .amount(history.getAmount()) // DTO amount is absolute
                                                .balanceAfter(history.getBalanceSnapshot())
                                                .ref(mapReference(history))
                                                .createdAt(history.getCreatedAt()
                                                                .atOffset(java.time.ZoneOffset.ofHours(9)))
                                                .build())
                                .toList();

                DepositHistoryResponse response = DepositHistoryResponse.builder()
                                .success(true)
                                .code("DEPOSIT_HISTORIES_RETRIEVED")
                                .message("예치금 내역을 조회했습니다.")
                                .data(DepositHistoryResponse.DepositHistoryData.builder()
                                                .items(items)
                                                .page(DepositHistoryResponse.PageInfo.builder()
                                                                .size(size)
                                                                .nextCursor(null) // Pagination 미구현
                                                                .build())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        private DepositHistoryResponse.ReferenceInfo mapReference(DepositHistoryDto history) {
                DepositHistoryResponse.ReferenceInfo.ReferenceInfoBuilder builder = DepositHistoryResponse.ReferenceInfo
                                .builder();

                if (history.getOrderItemUuid() != null) {
                        String type = history.getType().name();
                        if ("SETTLEMENT".equals(type)) {
                                builder.settlementUuid(history.getOrderItemUuid());
                        } else {
                                builder.orderUuid(history.getOrderItemUuid());
                        }
                }
                return builder.build();
        }

        /**
         * 전체 사용자 예치금 내역 조회 (관리자용)
         *
         * <p>
         * 관리자가 시스템 전체의 예치금 변동 내역을 조회한다.
         */
        @DepositApiDocs.GetAllHistories
        @GetMapping("/histories")
        public ResponseEntity<DepositHistoryResponse> getAllHistories(@RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) String cursor) {

                List<DepositHistoryDto> histories = depositFacade.findAllHistories();

                // TODO: Pagination Logic (현재는 전체 반환)
                List<DepositHistoryResponse.DepositHistoryHistoryItem> items = histories.stream()
                                .map(history -> DepositHistoryResponse.DepositHistoryHistoryItem.builder()
                                                .depositHistoryId(String.valueOf(history.getId()))
                                                .type(history.getType().name())
                                                .amount(history.getAmount())
                                                .balanceAfter(history.getBalanceSnapshot())
                                                .ref(mapReference(history))
                                                .createdAt(history.getCreatedAt()
                                                                .atOffset(java.time.ZoneOffset.ofHours(9)))
                                                .build())
                                .toList();

                DepositHistoryResponse response = DepositHistoryResponse.builder()
                                .success(true)
                                .code("DEPOSIT_HISTORIES_RETRIEVED")
                                .message("예치금 내역을 조회했습니다.")
                                .data(DepositHistoryResponse.DepositHistoryData.builder()
                                                .items(items)
                                                .page(DepositHistoryResponse.PageInfo.builder()
                                                                .size(size)
                                                                .nextCursor(null) // Pagination 미구현
                                                                .build())
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }
}
