package dukku.semicolon.shared.product.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

public final class SellerProductApiDocs {

    private SellerProductApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "판매자 상품 관리 API",
            description = "판매자 상품 등록/수정/삭제 (인증 필요: X-USER-UUID)"
    )
    public @interface SellerProductTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "판매자 상품 등록",
            description = "판매자가 상품을 등록합니다. (이미지 URL은 최대 10개, 순서대로 저장됩니다.)",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "임시 사용자 UUID 헤더",
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    )
            },
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Create Product",
                                    value = """
                                            {
                                              "categoryId": 1,
                                              "title": "빈티지 원목 독서대",
                                              "description": "생활 스크래치 약간 있어요.",
                                              "price": 45000,
                                              "shippingFee": 3000,
                                              "imageUrls": [
                                                "https://cdn.image.com/p/1024_1.png",
                                                "https://cdn.image.com/p/1024_2.png"
                                              ],
                                              "conditionStatus": "LIKE_NEW"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Created Product",
                                            value = """
                                                    {
                                                      "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                      "title": "빈티지 원목 독서대",
                                                      "description": "생활 스크래치 약간 있어요.",
                                                      "price": 45000,
                                                      "shippingFee": 3000,
                                                      "conditionStatus": "LIKE_NEW",
                                                      "saleStatus": "ON_SALE",
                                                      "visibilityStatus": "VISIBLE",
                                                      "likeCount": 0,
                                                      "viewCount": 0,
                                                      "imageUrls": [
                                                        "https://cdn.image.com/p/1024_1.png",
                                                        "https://cdn.image.com/p/1024_2.png"
                                                      ],
                                                      "category": { "id": 1, "name": "상의", "depth": 1 }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "카테고리를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 카테고리입니다."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(유효성 검증 실패 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "잘못된 요청",
                                                      "details": "title은 필수입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface CreateProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "판매자 상품 수정",
            description = """
                    판매자가 본인 상품을 수정합니다. (PATCH: 전달된 필드만 변경)
                    - imageUrls 제공 시: 전체 교체(순서 포함)
                    - imageUrls 미제공(null) 시: 기존 유지
                    - imageUrls 빈 배열([]) 시: 이미지 전체 삭제""",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "임시 사용자 UUID 헤더",
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    ),
                    @Parameter(
                            name = "productUuid",
                            in = ParameterIn.PATH,
                            required = true,
                            description = "상품 UUID",
                            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                    )
            },
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Update Title/Price",
                                            value = """
                                                    {
                                                      "title": "빈티지 원목 독서대(가격 인하)",
                                                      "price": 42000
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Replace Images (Reordered)",
                                            value = """
                                                    {
                                                      "imageUrls": [
                                                        "https://cdn.image.com/p/1024_2.png",
                                                        "https://cdn.image.com/p/1024_1.png",
                                                        "https://cdn.image.com/p/1024_3.png"
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Remove All Images",
                                            value = """
                                                    {
                                                      "imageUrls": []
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Updated Product",
                                            value = """
                                                    {
                                                      "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                      "title": "빈티지 원목 독서대(가격 인하)",
                                                      "description": "생활 스크래치 약간 있어요.",
                                                      "price": 42000,
                                                      "shippingFee": 3000,
                                                      "conditionStatus": "LIKE_NEW",
                                                      "saleStatus": "ON_SALE",
                                                      "visibilityStatus": "VISIBLE",
                                                      "likeCount": 0,
                                                      "viewCount": 0,
                                                      "imageUrls": [
                                                        "https://cdn.image.com/p/1024_2.png",
                                                        "https://cdn.image.com/p/1024_1.png",
                                                        "https://cdn.image.com/p/1024_3.png"
                                                      ],
                                                      "category": { "id": 1, "name": "상의", "depth": 1 }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음(본인 상품이 아님)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "Forbidden",
                                                      "details": "본인 상품만 수정할 수 있습니다."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음(삭제된 상품 포함)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 상품입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface UpdateProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "판매자 상품 삭제",
            description = "판매자가 본인 상품을 소프트 삭제합니다. (deletedAt 설정)",
            parameters = {
                    @Parameter(
                            name = "X-USER-UUID",
                            in = ParameterIn.HEADER,
                            required = true,
                            description = "임시 사용자 UUID 헤더",
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"
                    ),
                    @Parameter(
                            name = "productUuid",
                            in = ParameterIn.PATH,
                            required = true,
                            description = "상품 UUID",
                            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "상품 삭제 성공 (No Content)"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음(본인 상품이 아님)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "Forbidden",
                                                      "details": "본인 상품만 삭제할 수 있습니다."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음(삭제된 상품 포함)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 상품입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface DeleteProduct {}
}
