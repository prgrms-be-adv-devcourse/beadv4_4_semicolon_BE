package dukku.semicolon.shared.settlement.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 정산(Settlement) 관련 Swagger 전용 Meta-Annotation 모음
 */
public final class SettlementApiDocs {

    private SettlementApiDocs() {
    }

    // =============== 공통 태그 ===============
    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "정산 API", description = "정산 목록 조회, 통계 조회 관련 기능 (관리자 전용)")
    public @interface SettlementTag {
    }

    // =============== 1) 정산 목록 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 목록 조회", description = """
            관리자가 정산 내역을 검색 조건에 따라 조회합니다.

            - 정산 상태(status)로 필터링할 수 있습니다.
            - 특정 판매자(sellerUuid)의 정산만 조회할 수 있습니다.
            - 기간별(startDate ~ endDate) 정산 내역을 조회할 수 있습니다.
            - 페이징을 지원하며, 기본값은 20개씩 최신순으로 정렬됩니다.
            """, parameters = {
            @Parameter(name = "status", description = "정산 상태 (CREATED, PROCESSING, PENDING, SUCCESS, FAILED)", example = "SUCCESS"),
            @Parameter(name = "sellerUuid", description = "판매자 UUID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
            @Parameter(name = "startDate", description = "조회 시작일 (yyyy-MM-dd)", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일 (yyyy-MM-dd)", example = "2026-01-31"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "20"),
            @Parameter(name = "sort", description = "정렬 기준", example = "createdAt,desc")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Settlement List Response", value = """
                    {
                      "content": [
                        {
                          "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                          "status": "SUCCESS",
                          "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                          "sellerNickname": "홍길동상점",
                          "productName": "무선 이어폰",
                          "totalAmount": 50000,
                          "fee": 0.03,
                          "feeAmount": 1500,
                          "settlementAmount": 48500,
                          "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                          "bankName": "국민은행",
                          "accountNumber": "123-456-789012",
                          "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                        },
                        {
                          "settlementUuid": "e2d3c4b5-a6f7-8901-bcde-234567890abc",
                          "status": "PROCESSING",
                          "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                          "sellerNickname": "홍길동상점",
                          "productName": "스마트 워치",
                          "totalAmount": 120000,
                          "fee": 0.03,
                          "feeAmount": 3600,
                          "settlementAmount": 116400,
                          "settlementReservationDate": "2026-01-26T00:00:00+09:00",
                          "bankName": "국민은행",
                          "accountNumber": "123-456-789012",
                          "orderUuid": "c3e1f7d4-0d5f-55e2-af2f-9d3c4d8c2b22"
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 20,
                        "sort": {
                          "sorted": true,
                          "unsorted": false,
                          "empty": false
                        },
                        "offset": 0,
                        "paged": true,
                        "unpaged": false
                      },
                      "totalElements": 42,
                      "totalPages": 3,
                      "last": false,
                      "size": 20,
                      "number": 0,
                      "sort": {
                        "sorted": true,
                        "unsorted": false,
                        "empty": false
                      },
                      "numberOfElements": 20,
                      "first": true,
                      "empty": false
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_PARAMETER\", \"message\": \"시작일은 현재 또는 과거 날짜여야 합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface GetSettlements {
    }

    // =============== 2) 정산 단건 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 단건 조회", description = """
            관리자가 정산 UUID로 정산 상세 정보를 조회합니다.

            - 특정 정산의 상세 정보를 확인할 수 있습니다.
            - 정산 상태, 금액, 수수료, 판매자 정보 등을 조회할 수 있습니다.
            """, parameters = {
            @Parameter(name = "settlementUuid", description = "정산 UUID", example = "f1e2d3c4-b5a6-7890-cdef-1234567890ab", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Settlement Response", value = """
                    {
                      "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                      "status": "SUCCESS",
                      "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "sellerNickname": "홍길동상점",
                      "productName": "무선 이어폰",
                      "totalAmount": 50000,
                      "fee": 0.03,
                      "feeAmount": 1500,
                      "settlementAmount": 48500,
                      "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                      "bankName": "국민은행",
                      "accountNumber": "123-456-789012",
                      "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11",
                      "createdAt": "2026-01-22T14:30:00+09:00",
                      "updatedAt": "2026-01-25T01:05:00+09:00"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"SETTLEMENT_NOT_FOUND\", \"message\": \"정산을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface GetSettlement {
    }

    // =============== 3) 정산 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 통계 조회", description = """
            관리자가 정산 통계를 조회합니다.

            - 전체 정산 건수, 금액, 수수료, 정산액 통계를 제공합니다.
            - 상태별 건수와 금액 통계를 제공합니다.
            - 조회 기간 내 완료된 정산 통계를 제공합니다.
            - startDate와 endDate로 조회 기간을 지정할 수 있습니다.
            """, parameters = {
            @Parameter(name = "status", description = "정산 상태 필터 (CREATED, PROCESSING, PENDING, SUCCESS, FAILED)", example = "SUCCESS"),
            @Parameter(name = "sellerUuid", description = "판매자 UUID 필터", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
            @Parameter(name = "startDate", description = "조회 시작일 (yyyy-MM-dd)", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일 (yyyy-MM-dd)", example = "2026-01-31")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Settlement Statistics Response", value = """
                    {
                      "totalCount": 150,
                      "totalAmount": 7500000,
                      "totalSettlementAmount": 7275000,
                      "totalFeeAmount": 225000,
                      "createdCount": 10,
                      "processingCount": 25,
                      "pendingCount": 15,
                      "successCount": 95,
                      "failedCount": 5,
                      "createdAmount": 500000,
                      "processingAmount": 1250000,
                      "pendingAmount": 750000,
                      "successAmount": 4750000,
                      "failedAmount": 250000,
                      "completedCountInPeriod": 95,
                      "completedAmountInPeriod": 4750000
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_PARAMETER\", \"message\": \"시작일은 현재 또는 과거 날짜여야 합니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface GetSettlementStatistics {
    }
}
