package dukku.semicolon.shared.product.docs;

import io.swagger.v3.oas.annotations.Operation;
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

public final class CartApiDocs {

    private CartApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "장바구니 관리 API",
            description = "장바구니 추가, 조회, 삭제 관련 기능"
    )
    public @interface CartTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "장바구니에 상품 추가",
            description = "특정 상품을 장바구니에 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "장바구니 추가 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 상품을 찾을 수 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface CreateCart {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "장바구니의 상품 삭제",
            description = "장바구니에서 특정 상품을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "장바구니 상품 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "장바구니 상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"해당 장바구니 상품을 찾을 수 없습니다.\"}")
                            )
                    )
            }
    )
    public @interface DeleteCartItem {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "내 장바구니 조회",
            description = "현재 로그인한 사용자의 장바구니 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "장바구니 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "My Cart List",
                                            value = "{\n  \"items\": [\n    {\n      \"cartId\": 1,\n      \"product\": {\n        \"id\": 1,\n        \"name\": \"상품명\",\n        \"price\": 10000,\n        \"imageUrl\": \"url/to/image\"\n      },\n      \"quantity\": 2\n    }\n  ],\n  \"totalPrice\": 20000\n}"
                                    )
                            )
                    )
            }
    )
    public @interface FindMyCartList {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "장바구니 비우기",
            description = "현재 로그인한 사용자의 장바구니에 있는 모든 상품을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "장바구니 비우기 성공"
                    )
            }
    )
    public @interface DeleteAllCartItem {}
}
