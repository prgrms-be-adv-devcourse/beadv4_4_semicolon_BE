package dukku.semicolon.shared.payment.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 결제(Payment) 관련 Swagger 전용 Meta-Annotation 모음
 */
public final class PaymentApiDocs {

    private PaymentApiDocs() {
    }

    // =============== 공통 태그 ===============
    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "결제 API", description = "결제 요청, 승인, 조회, 환불 관련 기능")
    public @interface PaymentTag {
    }

    // =============== 1) 결제 요청 (준비) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "결제 요청 (준비)", description = """
            프론트에서 결제 준비 요청 시 토스 결제창 호출에 필요한 정보를 반환합니다.

            - 주문 정보와 금액을 검증합니다.
            - 예치금 잔액을 확인합니다.
            - 토스 결제창 호출에 필요한 orderId, successUrl, failUrl 등을 생성합니다.
            """, requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Payment Request", value = """
            {
              "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11",
              "couponUuid": null,
              "amounts": {
                "itemsTotalAmount": 15000,
                "couponDiscountAmount": 1500,
                "finalPayAmount": 13500,
                "depositUseAmount": 4500,
                "pgPayAmount": 9000
              },
              "orderName": "상품명 외 2건"
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "결제 준비 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Payment Response", value = """
                    {
                      "paymentUuid": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                      "status": "PENDING",
                      "toss": {
                        "orderId": "TOSS_9a5be1c6_20260114_001",
                        "amount": 9000,
                        "orderName": "상품명 외 2건",
                        "successUrl": "https://localhost:3000/payments/success?paymentUuid=...",
                        "failUrl": "https://localhost:3000/payments/fail?paymentUuid=..."
                      },
                      "amounts": {
                        "finalPayAmount": 13500,
                        "depositUseAmount": 4500,
                        "pgPayAmount": 9000
                      },
                      "createdAt": "2026-01-14T16:20:00+09:00"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "금액 불일치 또는 예치금 부족", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"AMOUNT_MISMATCH\", \"message\": \"결제 금액이 일치하지 않습니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "중복 요청 (멱등성 충돌)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"IDEMPOTENCY_CONFLICT\", \"message\": \"이미 처리된 결제 요청입니다.\"}")))
    })
    public @interface RequestPayment {
    }

    // =============== 2) 결제 승인 확정 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "결제 승인 확정", description = """
            토스 인증 완료 후 백엔드에서 최종 승인 처리를 합니다.

            - paymentKey, orderId, amount를 검증합니다.
            - 토스 Confirm API를 호출합니다.
            - 성공 시 결제 상태를 DONE으로 변경하고 예치금을 차감합니다.
            """, requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Confirm Request", value = """
            {
              "paymentUuid": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
              "toss": {
                "paymentKey": "tviva20260114pgkey_abcdef123456",
                "orderId": "TOSS_9a5be1c6_20260114_001",
                "amount": 9000
              }
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Confirm Response", value = """
                    {
                      "success": true,
                      "code": "PAYMENT_CONFIRMED",
                      "message": "결제가 승인되었습니다.",
                      "data": {
                        "paymentUuid": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                        "status": "DONE",
                        "approvedAt": "2026-01-14T16:22:10+09:00",
                        "toss": {
                          "orderId": "TOSS_9a5be1c6_20260114_001",
                          "paymentKey": "tviva20260114pgkey_abcdef123456"
                        },
                        "amounts": {
                          "finalPayAmount": 13500,
                          "depositUseAmount": 4500,
                          "pgPayAmount": 9000
                        }
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "금액 또는 orderId 불일치", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"TOSS_AMOUNT_MISMATCH\", \"message\": \"토스 결제 금액이 일치하지 않습니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "결제 상태가 PENDING이 아님", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"PAYMENT_NOT_PENDING\", \"message\": \"결제 상태가 승인 대기(PENDING)가 아닙니다.\"}"))),
            @ApiResponse(responseCode = "502", description = "토스 승인 API 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"TOSS_CONFIRM_FAILED\", \"message\": \"결제 승인에 실패했습니다.\"}")))
    })
    public @interface ConfirmPayment {
    }

    // =============== 3) 결제 내역 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "결제 내역 조회", description = "특정 결제의 상세 정보를 조회합니다.", parameters = {
            @Parameter(name = "paymentId", description = "결제 UUID", required = true, example = "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Result Response", value = """
                    {
                      "success": true,
                      "code": "PAYMENT_RESULT_RETRIEVED",
                      "message": "결제 내역을 조회했습니다.",
                      "data": {
                        "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                        "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11",
                        "status": "DONE",
                        "amounts": {
                          "totalAmount": 15000,
                          "couponDiscountAmount": 1500,
                          "depositUseAmount": 4500,
                          "pgPayAmount": 9000,
                          "finalPayAmount": 13500
                        },
                        "createdAt": "2026-01-14T16:20:00+09:00",
                        "approvedAt": "2026-01-14T16:22:10+09:00"
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "결제 내역 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"PAYMENT_NOT_FOUND\", \"message\": \"결제 내역을 찾을 수 없습니다.\"}")))
    })
    public @interface GetPaymentResult {
    }

    // =============== 4) 환불 요청 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "환불 요청", description = """
            결제 취소/환불을 요청합니다.

            - items가 있으면 부분 환불, 없으면 전체 환불로 처리됩니다.
            - 예치금 사용분은 예치금으로, PG 결제분은 PG로 환불됩니다.
            """, requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Refund Request", value = """
            {
              "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
              "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11",
              "refundAmount": 3750,
              "reason": "단순변심",
              "items": [
                {
                  "orderItemUuid": "f1b2c3d4-1111-2222-3333-444455556666",
                  "refundAmount": 2000
                }
              ]
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "환불 요청 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Refund Response", value = """
                    {
                      "success": true,
                      "code": "REFUND_REQUESTED",
                      "message": "환불 요청이 접수되었습니다.",
                      "data": {
                        "refundId": "3c5a8f20-2c4f-4b7a-9e5e-0a3e91e8a111",
                        "paymentId": "9a5be1c6-735e-4f69-a35f-7a9f6b0a9a9a",
                        "orderUuid": "b2f0f6d3-9c4f-44d1-9f1f-8c2b3c7b1a11",
                        "status": "CANCELED",
                        "amounts": {
                          "requestedRefundAmount": 3750,
                          "depositRefundAmount": 1000,
                          "pgRefundAmount": 2750
                        },
                        "pg": {
                          "provider": "TOSS_PAYMENTS",
                          "tossOrderId": "TOSS_9a5be1c6_20260114_001",
                          "cancelTransactionKey": "8B3D...TXN..."
                        },
                        "createdAt": "2026-01-14T16:40:00+09:00",
                        "completedAt": "2026-01-14T16:40:02+09:00"
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "환불 불가 상태", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"PAYMENT_NOT_REFUNDABLE\", \"message\": \"해당 결제는 환불할 수 없는 상태입니다.\"}"))),
            @ApiResponse(responseCode = "422", description = "환불 금액 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"INVALID_REFUND_AMOUNT\", \"message\": \"유효하지 않은 환불 금액입니다.\"}"))),
            @ApiResponse(responseCode = "502", description = "PG 취소 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"PG_CANCEL_FAILED\", \"message\": \"PG 결제 취소에 실패했습니다.\"}")))
    })
    public @interface RefundPayment {
    }
}
