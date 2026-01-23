package dukku.semicolon.shared.product.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

public final class ShopApiDocs {

    private ShopApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "상점(판매자) API",
            description = "내 상점 조회/수정 및 판매자 상점/상품 목록 조회"
    )
    public @interface ShopTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "내 상점 조회",
            description = "로그인한 사용자의 상점 정보를 조회합니다.",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            description = "임시 사용자 UUID 헤더",
                            required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 상점 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "My Shop",
                                            value = """
                                                    {
                                                      "shopUuid": "11111111-2222-3333-4444-555555555555",
                                                      "intro": "안녕하세요! 빈티지 소품 위주로 판매해요.",
                                                      "salesCount": 10,
                                                      "activeListingCount": 3
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindMyShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "내 상점 소개 수정",
            description = "내 상점 소개글(intro)을 수정합니다.",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            description = "임시 사용자 UUID 헤더",
                            required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Shop Intro",
                                    value = """
                                            {
                                              "intro": "상점 소개를 이렇게 바꿨어요!"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 상점 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Updated Shop",
                                            value = """
                                                    {
                                                      "shopUuid": "11111111-2222-3333-4444-555555555555",
                                                      "intro": "상점 소개를 이렇게 바꿨어요!",
                                                      "salesCount": 10,
                                                      "activeListingCount": 3
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface UpdateMyShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "내 상점 상품 목록 조회",
            description = "로그인한 사용자의 상점(내 판매 상품) 목록을 페이징으로 조회합니다. (saleStatus로 필터 가능)",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            description = "임시 사용자 UUID 헤더",
                            required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    ),
                    @Parameter(
                            name = "saleStatus",
                            in = ParameterIn.QUERY,
                            description = "판매 상태 필터(선택): ON_SALE | RESERVED | SOLD_OUT",
                            example = "ON_SALE"
                    ),
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "페이지(0부터 시작, 기본 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            description = "페이지 사이즈(기본 20, 최대 50)",
                            example = "20"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 상점 상품 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "My Shop Products",
                                            value = """
                                                    {
                                                      "items": [
                                                        {
                                                          "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                          "title": "빈티지 원목 독서대",
                                                          "price": 45000,
                                                          "thumbnailUrl": "https://cdn.image.com/p/1024_thumb.png",
                                                          "likeCount": 150
                                                        }
                                                      ],
                                                      "page": 0,
                                                      "size": 20,
                                                      "totalCount": 7,
                                                      "hasNext": false
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindMyShopProducts {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "판매자 상점 조회(공개)",
            description = "판매자 상점 UUID로 상점 정보를 조회합니다.",
            parameters = {
                    @Parameter(
                            name = "shopUuid",
                            in = ParameterIn.PATH,
                            description = "상점 UUID",
                            required = true,
                            example = "11111111-2222-3333-4444-555555555555"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상점 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Shop",
                                            value = """
                                                    {
                                                      "shopUuid": "11111111-2222-3333-4444-555555555555",
                                                      "intro": "안녕하세요! 빈티지 소품 위주로 판매해요.",
                                                      "salesCount": 10,
                                                      "activeListingCount": 3
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상점을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 상점입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "판매자 상점 상품 목록 조회(공개)",
            description = "판매자 상점의 상품 목록을 페이징으로 조회합니다. (saleStatus로 필터 가능)",
            parameters = {
                    @Parameter(
                            name = "shopUuid",
                            in = ParameterIn.PATH,
                            description = "상점 UUID",
                            required = true,
                            example = "11111111-2222-3333-4444-555555555555"
                    ),
                    @Parameter(
                            name = "saleStatus",
                            in = ParameterIn.QUERY,
                            description = "판매 상태 필터(선택): ON_SALE | RESERVED | SOLD_OUT",
                            example = "ON_SALE"
                    ),
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "페이지(0부터 시작, 기본 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            description = "페이지 사이즈(기본 20, 최대 50)",
                            example = "20"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상점 상품 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "Shop Products",
                                                    value = """
                                                            {
                                                              "items": [
                                                                {
                                                                  "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                                  "title": "빈티지 원목 독서대",
                                                                  "price": 45000,
                                                                  "thumbnailUrl": "https://cdn.image.com/p/1024_thumb.png",
                                                                  "likeCount": 150
                                                                }
                                                              ],
                                                              "page": 0,
                                                              "size": 20,
                                                              "totalCount": 42,
                                                              "hasNext": true
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(saleStatus 값이 유효하지 않음 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "잘못된 요청",
                                                      "details": "saleStatus 값이 유효하지 않습니다."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상점을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 상점입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindShopProducts {}
}
