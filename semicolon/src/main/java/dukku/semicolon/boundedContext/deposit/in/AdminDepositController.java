package dukku.semicolon.boundedContext.deposit.in;

import dukku.semicolon.shared.deposit.docs.DepositApiDocs;
import dukku.semicolon.shared.deposit.dto.DepositBalanceResponse;
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

        /**
         * 특정 사용자 예치금 잔액 조회 (관리자용)
         */
        @DepositApiDocs.GetUserBalance
        @GetMapping("/{userUuid}/balance")
        public ResponseEntity<DepositBalanceResponse> getUserBalance(
                        @PathVariable UUID userUuid) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                DepositBalanceResponse response = DepositBalanceResponse.builder()
                                .success(true)
                                .code("DEPOSIT_BALANCE_RETRIEVED")
                                .message("예치금 잔액을 조회했습니다.")
                                .data(DepositBalanceResponse.DepositBalanceData.builder()
                                                .userUuid(userUuid)
                                                .balance(50000)
                                                .updatedAt(OffsetDateTime.now().minusDays(1))
                                                .build())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * 특정 사용자 예치금 내역 조회 (관리자용)
         */
        @DepositApiDocs.GetUserHistories
        @GetMapping("/{userUuid}/histories")
        public ResponseEntity<DepositHistoryResponse> getUserHistories(
                        @PathVariable UUID userUuid,
                        @RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) String cursor) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                DepositHistoryResponse response = createDummyHistoryResponse(size, "DEPOSIT_HISTORIES_RETRIEVED",
                                "예치금 내역을 조회했습니다.");

                return ResponseEntity.ok(response);
        }

        /**
         * 전체 사용자 예치금 내역 조회 (관리자용)
         */
        @DepositApiDocs.GetAllHistories
        @GetMapping("/histories")
        public ResponseEntity<DepositHistoryResponse> getAllHistories(
                        @RequestParam(defaultValue = "20") Integer size,
                        @RequestParam(required = false) String cursor) {

                // TODO: 서비스 연결 예정 - 현재는 더미 응답
                DepositHistoryResponse response = createDummyHistoryResponse(size, "DEPOSIT_HISTORIES_RETRIEVED",
                                "예치금 내역을 조회했습니다.");

                return ResponseEntity.ok(response);
        }

        private DepositHistoryResponse createDummyHistoryResponse(Integer size, String code, String message) {
                return DepositHistoryResponse.builder()
                                .success(true)
                                .code(code)
                                .message(message)
                                .data(DepositHistoryResponse.DepositHistoryData.builder()
                                                .items(List.of(
                                                                DepositHistoryResponse.DepositHistoryHistoryItem
                                                                                .builder()
                                                                                .depositHistoryId(
                                                                                                "b7c7b8c9-6c7a-4a8b-b8c2-1a2b3c4d5e6f")
                                                                                .type("USE")
                                                                                .amount(-4500)
                                                                                .balanceAfter(12500)
                                                                                .ref(DepositHistoryResponse.ReferenceInfo
                                                                                                .builder()
                                                                                                .paymentId(UUID.fromString(
                                                                                                                "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a"))
                                                                                                .orderUuid(UUID.fromString(
                                                                                                                "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"))
                                                                                                .build())
                                                                                .createdAt(OffsetDateTime.now()
                                                                                                .minusMinutes(12))
                                                                                .build()))
                                                .page(DepositHistoryResponse.PageInfo.builder()
                                                                .size(size)
                                                                .nextCursor("2026-01-15T13:58:00+09:00|"
                                                                                + UUID.randomUUID())
                                                                .build())
                                                .build())
                                .build();
        }
}
