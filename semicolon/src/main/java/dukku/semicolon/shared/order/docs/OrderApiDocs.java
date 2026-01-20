package dukku.semicolon.shared.order.docs;

import io.swagger.v3.oas.annotations.Operation;
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

public final class OrderApiDocs {

    private OrderApiDocs() {}

    // =============== 공통 태그 ===============
    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "주문 관리 API",
            description = "주문 생성, 조회, 수정 관련 기능"
    )
    public @interface OrderTag {}

    // =============== 1) 주문 생성 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "주문 생성",
            description = "새로운 주문을 생성합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Order Request",
                                    value = """
                                            {
                                              "orderedBy": "550e8400-e29b-41d4-a716-446655440000",
                                              "receiverName": "김세미",
                                              "receiverPhone": "010-1234-5678",
                                              "receiverAddress": "서울시 강남구",
                                              "receiverPostalCode": "12345",
                                              "orderItems": [
                                                {
                                                  "productId": 1,
                                                  "quantity": 2
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "주문 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = """
                                                    {
                                                      "orderUuid": "550e8400-e29b-41d4-a716-446655440001",
                                                      "orderedBy": "550e8400-e29b-41d4-a716-446655440000",
                                                      "receiverName": "김세미",
                                                      "receiverPhone": "010-1234-5678",
                                                      "receiverAddress": "서울시 강남구",
                                                      "receiverPostalCode": "12345",
                                                      "totalAmount": 25000,
                                                      "orderStatus": "PENDING",
                                                      "orderItems": [
                                                        {
                                                          "orderItemUuid": "550e8400-e29b-41d4-a716-446655440002",
                                                          "productId": 1,
                                                          "productName": "상품명",
                                                          "quantity": 2,
                                                          "price": 10000,
                                                          "orderItemStatus": "PENDING",
                                                          "deliveryTrackingNumber": null
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (유효성 검사 실패 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"주문 정보가 올바르지 않습니다.\"}")
                            )
                    )
            }
    )
    public @interface CreateOrder {}

    // =============== 2) 주문 상세 조회 (관리자 또는 사용자) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "주문 상세 조회",
            description = "특정 주문의 상세 정보를 조회합니다. (관리자 또는 해당 주문 사용자만 가능)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주문 상세 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Order Details",
                                            value = """
                                                    {
                                                      "orderUuid": "550e8400-e29b-41d4-a716-446655440001",
                                                      "orderedBy": "550e8400-e29b-41d4-a716-446655440000",
                                                      "receiverName": "김세미",
                                                      "receiverPhone": "010-1234-5678",
                                                      "receiverAddress": "서울시 강남구",
                                                      "receiverPostalCode": "12345",
                                                      "totalAmount": 25000,
                                                      "orderStatus": "PENDING",
                                                      "orderItems": [
                                                        {
                                                          "orderItemUuid": "550e8400-e29b-41d4-a716-446655440002",
                                                          "productId": 1,
                                                          "productName": "상품명",
                                                          "quantity": 2,
                                                          "price": 10000,
                                                          "orderItemStatus": "PENDING",
                                                          "deliveryTrackingNumber": null
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "주문을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문을 찾을 수 없습니다.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "접근 권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문에 접근할 권한이 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface FindOrderByUuid {}

    // =============== 3) 배송지 정보 수정 (사용자) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "배송지 정보 수정",
            description = "사용자가 본인의 주문에 대한 배송지 정보를 수정합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Shipping Info Request",
                                    value = """
                                            {
                                              "receiverName": "김세미",
                                              "receiverPhone": "010-9876-5432",
                                              "receiverAddress": "서울시 강남구",
                                              "receiverPostalCode": "12345"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "배송지 정보 수정 성공 (No Content)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "주문을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문을 찾을 수 없습니다.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "접근 권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문에 접근할 권한이 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface UpdateShippingInfo {}

    // =============== 4) 관리자 주문 목록 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "관리자 주문 목록 조회",
            description = "관리자가 모든 주문 목록을 조건에 따라 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주문 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Admin Order List",
                                            value = """
                                                    {
                                                      "content": [
                                                        {
                                                          "orderUuid": "550e8400-e29b-41d4-a716-446655440001",
                                                          "orderedBy": "550e8400-e29b-41d4-a716-446655440000",
                                                          "totalAmount": 25000,
                                                          "orderStatus": "PENDING"
                                                        }
                                                      ],
                                                      "pageable": {
                                                        "sort": {
                                                          "empty": true,
                                                          "sorted": false,
                                                          "unsorted": true
                                                        },
                                                        "offset": 0,
                                                        "pageNumber": 0,
                                                        "pageSize": 10,
                                                        "paged": true,
                                                        "unpaged": false
                                                      },
                                                      "last": true,
                                                      "totalElements": 1,
                                                      "totalPages": 1,
                                                      "size": 10,
                                                      "number": 0,
                                                      "sort": {
                                                        "empty": true,
                                                        "sorted": false,
                                                        "unsorted": true
                                                      },
                                                      "first": true,
                                                      "numberOfElements": 1,
                                                      "empty": false
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "접근 권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"관리자만 접근 가능합니다.\"}")
                            )
                    )
            }
    )
    public @interface FindAdminOrderList {}

    // =============== 5) 본인 주문 목록 조회 (사용자) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "본인 주문 목록 조회",
            description = "현재 로그인한 사용자의 주문 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주문 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "My Order List",
                                            value = """
                                                    {
                                                      "content": [
                                                        {
                                                          "orderUuid": "550e8400-e29b-41d4-a716-446655440001",
                                                          "orderedBy": "550e8400-e29b-41d4-a716-446655440000",
                                                          "totalAmount": 25000,
                                                          "orderStatus": "PENDING"
                                                        }
                                                      ],
                                                      "pageable": {
                                                        "sort": {
                                                          "empty": true,
                                                          "sorted": false,
                                                          "unsorted": true
                                                        },
                                                        "offset": 0,
                                                        "pageNumber": 0,
                                                        "pageSize": 10,
                                                        "paged": true,
                                                        "unpaged": false
                                                      },
                                                      "last": true,
                                                      "totalElements": 1,
                                                      "totalPages": 1,
                                                      "size": 10,
                                                      "number": 0,
                                                      "sort": {
                                                        "empty": true,
                                                        "sorted": false,
                                                        "unsorted": true
                                                      },
                                                      "first": true,
                                                      "numberOfElements": 1,
                                                      "empty": false
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindMyOrderList {}

    // =============== 6) 주문 상품 배송 정보 입력 (판매자) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "주문 상품 배송 정보 입력",
            description = "주문 상품에 대한 운송장 번호 등 배송 정보를 입력합니다. (판매자만 가능)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Delivery Info Request",
                                    value = """
                                            {
                                              "deliveryCompany": "CJ대한통운",
                                              "deliveryTrackingNumber": "123456789012"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "배송 정보 입력 성공 (No Content)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "주문 상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문 상품을 찾을 수 없습니다.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "접근 권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문 상품에 접근할 권한이 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface UpdateOrderItemDeliveryInfo {}

    // =============== 7) 주문 상품 상태 변경 (사용자/관리자) ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "주문 상품 상태 변경",
            description = "주문 상품의 상태를 변경합니다. (구매 확정, 취소 요청, 환불 요청 등)",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "주문 상품 상태 변경 성공 (No Content)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "주문 상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문 상품을 찾을 수 없습니다.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "접근 권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 주문 상품에 접근할 권한이 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface UpdateOrderItemStatus {}
}
