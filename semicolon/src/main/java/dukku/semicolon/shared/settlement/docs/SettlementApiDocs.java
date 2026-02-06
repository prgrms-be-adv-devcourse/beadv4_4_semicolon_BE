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
            @Parameter(name = "status", description = "정산 상태 (PENDING, PROCESSING, SUCCESS, FAILED)", example = "SUCCESS"),
            @Parameter(name = "sellerUuid", description = "판매자 UUID (홍길동상점)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
            @Parameter(name = "startDate", description = "조회 시작일 (yyyy-MM-dd)", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일 (yyyy-MM-dd)", example = "2026-02-28"),
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
            @Parameter(name = "status", description = "정산 상태 필터 (PENDING, PROCESSING, SUCCESS, FAILED)", example = "SUCCESS"),
            @Parameter(name = "sellerUuid", description = "판매자 UUID 필터 (홍길동상점)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
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

    // =============== 4) 배치 Job 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "배치 Job 통계 조회", description = """
            기간 내 배치 Job 실행 통계를 조회합니다.

            - 배치 실행 현황 (Job별, 상태별 건수)
            - 실패한 배치 목록
            - 재시작 가능한 Job 목록
            - 가장 많이 발생하는 에러 TOP 10
            - 일별 정산 처리 건수
            """, parameters = {
            @Parameter(name = "startDate", description = "조회 시작일 (yyyy-MM-dd, 기본값: 30일 전)", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일 (yyyy-MM-dd, 기본값: 오늘)", example = "2026-01-31")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public @interface GetBatchJobStatistics {
    }

    // =============== 5) 배치 Step 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "배치 Step 통계 조회", description = """
            기간 내 배치 Step 성능 통계를 조회합니다.

            - Step별 평균 처리 시간
            - Step별 평균 읽기/쓰기/스킵 건수
            - Step별 총 실행 횟수
            """, parameters = {
            @Parameter(name = "startDate", description = "조회 시작일", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일", example = "2026-01-31")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public @interface GetBatchStepStatistics {
    }

    // =============== 6) 재무 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "재무 통계 조회", description = """
            정산 관련 재무 통계를 조회합니다.

            - 플랫폼 수익 (수수료 총합)
            - 정산 대기/처리 중/실패 금액
            - 총 거래액, 총 정산 완료 금액
            - 평균 수수료율
            """, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public @interface GetFinancialStatistics {
    }

    // =============== 7) 트렌드 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "트렌드 통계 조회", description = """
            기간 내 정산 트렌드 통계를 조회합니다.

            - 일별 정산 금액 추이
            - 월별 정산 금액 추이
            - 처리 시간 분석 (평균/최소/최대)
            """, parameters = {
            @Parameter(name = "startDate", description = "조회 시작일", example = "2026-01-01"),
            @Parameter(name = "endDate", description = "조회 종료일", example = "2026-01-31")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public @interface GetTrendStatistics {
    }

    // =============== 8) 판매자별 정산 통계 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "판매자별 정산 통계 조회", description = """
            관리자가 판매자별 정산 통계를 조회합니다.

            - 판매자별 총 정산 건수, 성공/실패/대기 건수
            - 판매자별 정산 성공률 (SUCCESS / 전체)
            - 판매자별 총 정산 금액, 수수료 금액
            - 정산 건수 기준 내림차순 정렬
            - 페이징을 지원합니다.
            """, parameters = {
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "20")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Seller Statistics Response", value = """
                    {
                      "sellerSummaries": [
                        {
                          "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                          "totalSettlementCount": 50,
                          "successCount": 45,
                          "failedCount": 3,
                          "pendingCount": 2,
                          "successRate": 90.00,
                          "totalSettledAmount": 2250000,
                          "totalFeeAmount": 112500
                        },
                        {
                          "sellerUuid": "b2c3d4e5-f6a7-8901-bcde-f2345678901a",
                          "totalSettlementCount": 30,
                          "successCount": 28,
                          "failedCount": 1,
                          "pendingCount": 1,
                          "successRate": 93.33,
                          "totalSettledAmount": 1400000,
                          "totalFeeAmount": 70000
                        }
                      ],
                      "totalSellerCount": 25,
                      "totalPages": 2
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface GetSellerStatistics {
    }

    // =============== 6) 실패한 정산 재처리 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "실패한 정산 재처리", description = """
            관리자가 실패한 정산을 재처리 대기 상태로 변경합니다.

            - 정산 상태가 FAILED인 경우에만 사용 가능합니다.
            - 상태가 PENDING으로 변경되어 다음 배치 처리 시 재시도됩니다.
            """, parameters = {
            @Parameter(name = "settlementUuid", description = "정산 UUID", example = "f1e2d3c4-b5a6-7890-cdef-1234567890ab", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "재처리 요청 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Retry Settlement Response", value = """
                    {
                      "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                      "status": "PENDING",
                      "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "totalAmount": 50000,
                      "fee": 0.03,
                      "feeAmount": 1500,
                      "settlementAmount": 48500,
                      "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                      "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "상태 전이 불가", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_STATUS_TRANSITION\", \"message\": \"정산 상태를 PENDING에서 PENDING으로 변경할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"SETTLEMENT_NOT_FOUND\", \"message\": \"정산을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface RetrySettlement {
    }

    // =============== 5) 정산 수동 완료 처리 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 수동 완료 처리", description = """
            관리자가 정산을 수동으로 완료 처리합니다.

            - 정산 상태가 PROCESSING인 경우에만 사용 가능합니다.
            - 예치금 충전이 외부에서 확인되었으나 시스템에 반영되지 않은 경우 사용합니다.
            - 상태가 SUCCESS로 변경되고 completedAt이 현재 시간으로 설정됩니다.
            """, parameters = {
            @Parameter(name = "settlementUuid", description = "정산 UUID", example = "f1e2d3c4-b5a6-7890-cdef-1234567890ab", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "완료 처리 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Complete Settlement Response", value = """
                    {
                      "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                      "status": "SUCCESS",
                      "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "totalAmount": 50000,
                      "fee": 0.03,
                      "feeAmount": 1500,
                      "settlementAmount": 48500,
                      "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                      "completedAt": "2026-01-25T10:30:00+09:00",
                      "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "상태 전이 불가", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_STATUS_TRANSITION\", \"message\": \"정산 상태를 PENDING에서 SUCCESS로 변경할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"SETTLEMENT_NOT_FOUND\", \"message\": \"정산을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface CompleteSettlement {
    }

    // =============== 6) 정산 수동 실패 처리 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 수동 실패 처리", description = """
            관리자가 정산을 수동으로 실패 처리합니다.

            - 정산 상태가 PROCESSING인 경우에만 사용 가능합니다.
            - 예치금 충전이 실패했으나 시스템에 반영되지 않은 경우 사용합니다.
            - 상태가 FAILED로 변경됩니다.
            """, parameters = {
            @Parameter(name = "settlementUuid", description = "정산 UUID", example = "f1e2d3c4-b5a6-7890-cdef-1234567890ab", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "실패 처리 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Fail Settlement Response", value = """
                    {
                      "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                      "status": "FAILED",
                      "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "totalAmount": 50000,
                      "fee": 0.03,
                      "feeAmount": 1500,
                      "settlementAmount": 48500,
                      "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                      "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "상태 전이 불가", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_STATUS_TRANSITION\", \"message\": \"정산 상태를 PENDING에서 FAILED로 변경할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"SETTLEMENT_NOT_FOUND\", \"message\": \"정산을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface FailSettlement {
    }

    // =============== 7) 정산 수동 예치금 충전 요청 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "정산 수동 예치금 충전 요청", description = """
            관리자가 정산을 수동으로 예치금 충전 요청합니다.

            - 정산 상태가 PENDING인 경우에만 사용 가능합니다.
            - 배치 처리를 기다리지 않고 즉시 예치금 충전을 요청합니다.
            - 상태가 PROCESSING으로 변경되고 예치금 충전 이벤트가 발행됩니다.
            """, parameters = {
            @Parameter(name = "settlementUuid", description = "정산 UUID", example = "f1e2d3c4-b5a6-7890-cdef-1234567890ab", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "처리 요청 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Process Settlement Response", value = """
                    {
                      "settlementUuid": "f1e2d3c4-b5a6-7890-cdef-1234567890ab",
                      "status": "PROCESSING",
                      "sellerUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "totalAmount": 50000,
                      "fee": 0.03,
                      "feeAmount": 1500,
                      "settlementAmount": 48500,
                      "settlementReservationDate": "2026-01-25T00:00:00+09:00",
                      "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "상태 전이 불가 또는 유효성 검증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_STATUS_TRANSITION\", \"message\": \"정산 상태를 PROCESSING에서 PROCESSING으로 변경할 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "정산을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"SETTLEMENT_NOT_FOUND\", \"message\": \"정산을 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"FORBIDDEN\", \"message\": \"관리자만 접근 가능합니다.\"}")))
    })
    public @interface ProcessSettlement {
    }
}
