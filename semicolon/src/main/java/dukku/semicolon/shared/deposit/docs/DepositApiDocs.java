package dukku.semicolon.shared.deposit.docs;

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
 * 예치금(Deposit) 관련 Swagger 전용 Meta-Annotation 모음
 */
public final class DepositApiDocs {

  private DepositApiDocs() {
  }

  // =============== 공통 태그 ===============
  @Documented
  @Target(TYPE)
  @Retention(RUNTIME)
  @Tag(name = "예치금 API", description = "예치금 잔액 조회, 변동 내역 조회 관련 기능")
  public @interface DepositTag {
  }

  // =============== 1) 내 예치금 잔액 조회 ===============
  @Documented
  @Target(METHOD)
  @Retention(RUNTIME)
  @Operation(summary = "내 예치금 잔액 조회", description = "현재 로그인한 사용자의 예치금 잔액을 조회합니다. (GET /api/v1/deposits/me/balance)", responses = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Balance Response", value = """
          {
            "success": true,
            "code": "DEPOSIT_BALANCE_RETRIEVED",
            "message": "예치금 잔액을 조회했습니다.",
            "data": {
              "userUuid": "0b6f5f1a-9c64-4ac1-9a51-2f8b2f1a9d12",
              "balance": 12500,
              "updatedAt": "2026-01-15T14:10:00+09:00"
            }
          }
          """))),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "404", description = "예치금 정보 없음")
  })
  public @interface GetMyBalance {
  }

  // =============== 2) 내 예치금 변동 내역 조회 ===============
  @Documented
  @Target(METHOD)
  @Retention(RUNTIME)
  @Operation(summary = "내 예치금 변동 내역 조회", description = "현재 로그인한 사용자의 예치금 변동 이력을 조회합니다. (GET /api/v1/deposits/me/histories)", parameters = {
      @Parameter(name = "size", description = "페이지 크기", example = "20"),
      @Parameter(name = "cursor", description = "다음 페이지 커서 (createdAt|historyId)")
  }, responses = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "History Response", value = """
          {
            "success": true,
            "code": "DEPOSIT_HISTORIES_RETRIEVED",
            "message": "예치금 내역을 조회했습니다.",
            "data": {
              "items": [
                {
                  "depositHistoryId": "b7c7b8c9-6c7a-4a8b-b8c2-1a2b3c4d5e6f",
                  "type": "USE",
                  "amount": -4500,
                  "balanceAfter": 12500,
                  "ref": {
                    "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                    "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                  },
                  "createdAt": "2026-01-15T13:58:00+09:00"
                }
              ],
              "page": {
                "size": 20,
                "nextCursor": "2026-01-15T13:58:00+09:00|b7c7b8c9-..."
              }
            }
          }
          """))),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public @interface GetMyHistories {
  }

  // =============== 3) 특정 사용자 예치금 잔액 조회 (관리자) ===============
  @Documented
  @Target(METHOD)
  @Retention(RUNTIME)
  @Operation(summary = "특정 사용자 예치금 잔액 조회 (관리자)", description = "관리자가 특정 사용자의 예치금 잔액을 조회합니다. (GET /api/v1/admin/deposits/{userUuid}/balance)", parameters = {
      @Parameter(name = "userUuid", description = "사용자 UUID", required = true, example = "0b6f5f1a-9c64-4ac1-9a51-2f8b2f1a9d12")
  }, responses = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Admin Balance Response", value = """
          {
            "success": true,
            "code": "DEPOSIT_BALANCE_RETRIEVED",
            "message": "예치금 잔액을 조회했습니다.",
            "data": {
              "userUuid": "0b6f5f1a-9c64-4ac1-9a51-2f8b2f1a9d12",
              "balance": 12500,
              "updatedAt": "2026-01-15T14:10:00+09:00"
            }
          }
          """))),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 또는 예치금 정보 없음")
  })
  public @interface GetUserBalance {
  }

  // =============== 4) 특정 사용자 예치금 내역 조회 (관리자) ===============
  @Documented
  @Target(METHOD)
  @Retention(RUNTIME)
  @Operation(summary = "특정 사용자 예치금 내역 조회 (관리자)", description = "관리자가 특정 사용자의 예치금 변동 이력을 조회합니다. (GET /api/v1/admin/deposits/{userUuid}/histories)", parameters = {
      @Parameter(name = "userUuid", description = "사용자 UUID", required = true, example = "0b6f5f1a-9c64-4ac1-9a51-2f8b2f1a9d12"),
      @Parameter(name = "size", description = "페이지 크기", example = "20"),
      @Parameter(name = "cursor", description = "다음 페이지 커서")
  }, responses = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Admin User History Response", value = """
          {
            "success": true,
            "code": "DEPOSIT_HISTORIES_RETRIEVED",
            "message": "예치금 내역을 조회했습니다.",
            "data": {
              "items": [
                {
                  "depositHistoryId": "b7c7b8c9-6c7a-4a8b-b8c2-1a2b3c4d5e6f",
                  "type": "USE",
                  "amount": -4500,
                  "balanceAfter": 12500,
                  "ref": {
                    "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                    "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                  },
                  "createdAt": "2026-01-15T13:58:00+09:00"
                }
              ],
              "page": {
                "size": 20,
                "nextCursor": "2026-01-15T13:58:00+09:00|b7c7b8c9-..."
              }
            }
          }
          """))),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public @interface GetUserHistories {
  }

  // =============== 5) 전체 사용자 예치금 내역 조회 (관리자) ===============
  @Documented
  @Target(METHOD)
  @Retention(RUNTIME)
  @Operation(summary = "전체 사용자 예치금 내역 조회 (관리자)", description = "관리자가 시스템 전체 예치금 변동 이력을 조회합니다. (GET /api/v1/admin/deposits/histories)", parameters = {
      @Parameter(name = "size", description = "페이지 크기", example = "20"),
      @Parameter(name = "cursor", description = "다음 페이지 커서")
  }, responses = {
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Admin All History Response", value = """
          {
            "success": true,
            "code": "DEPOSIT_HISTORIES_RETRIEVED",
            "message": "예치금 내역을 조회했습니다.",
            "data": {
              "items": [
                {
                  "depositHistoryId": "b7c7b8c9-6c7a-4a8b-b8c2-1a2b3c4d5e6f",
                  "type": "USE",
                  "amount": -4500,
                  "balanceAfter": 12500,
                  "ref": {
                    "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                    "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11"
                  },
                  "createdAt": "2026-01-15T13:58:00+09:00"
                }
              ],
              "page": {
                "size": 20,
                "nextCursor": "2026-01-15T13:58:00+09:00|b7c7b8c9-..."
              }
            }
          }
          """))),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public @interface GetAllHistories {
  }
}
