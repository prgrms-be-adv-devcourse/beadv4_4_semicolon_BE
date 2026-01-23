package dukku.semicolon.shared.product.docs;

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

public final class ProductApiDocs {

    private ProductApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "상품 조회 API",
            description = "카테고리 조회, 메인 추천(주목) 상품, 상품 목록/상세 조회"
    )
    public @interface ProductTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "카테고리 목록 조회",
            description = "상품 카테고리 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Category List",
                                            value =
                                                    """
                                                            [
                                                              {"categoryId": 1, "categoryName": "상의", "depth": 1, "parentId": null},
                                                              {"categoryId": 2, "categoryName": "후드", "depth": 2, "parentId": 1}
                                                            ]"""
                                    )
                            )
                    )
            }
    )
    public @interface FindCategories {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "메인 주목(추천) 상품 조회",
            description = "메인 화면에서 보여줄 주목 상품 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "size", description = "조회할 상품 개수(기본 20)", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주목 상품 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Featured Products",
                                            value =
                                                    """
                                                            [
                                                              {
                                                                "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                                "title": "오프화이트 후드",
                                                                "price": 120000,
                                                                "thumbnailUrl": "https://image.../1.jpg",
                                                                "likeCount": 15
                                                              }
                                                            ]"""
                                    )
                            )
                    )
            }
    )
    public @interface FindFeaturedProducts {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "상품 목록 조회",
            description = "카테고리/정렬/페이징 조건으로 상품 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "categoryId", description = "카테고리 ID(선택)", example = "1"),
                    @Parameter(name = "sort", description = "정렬 기준(recent|popular)", example = "recent"),
                    @Parameter(name = "page", description = "페이지(0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지 사이즈", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Product List",
                                            value =
                                                    """
                                                            {
                                                              "page": 0,
                                                              "size": 20,
                                                              "totalElements": 1,
                                                              "items": [
                                                                {
                                                                  "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                                  "title": "오프화이트 후드",
                                                                  "price": 120000,
                                                                  "thumbnailUrl": "https://image.../1.jpg",
                                                                  "likeCount": 15
                                                                }
                                                              ]
                                                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(정렬 값 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value =
                                                    """
                                                            {
                                                              "message": "잘못된 요청",
                                                              "details": "sort 값이 유효하지 않습니다."
                                                            }"""
                                    )
                            )
                    )
            }
    )
    public @interface FindProductList {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "상품 상세 조회",
            description = "상품 UUID로 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 상세 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Product Detail",
                                            value =
                                                    "{ " +
                                                            "\"productUuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\", " +
                                                            "\"title\": \"오프화이트 후드\", " +
                                                            "\"description\": \"상태 좋아요\", " +
                                                            "\"price\": 120000, " +
                                                            "\"shippingFee\": 3000, " +
                                                            "\"conditionStatus\": \"SEALED\", " +
                                                            "\"saleStatus\": \"ON_SALE\", " +
                                                            "\"visibilityStatus\": \"VISIBLE\", " +
                                                            "\"likeCount\": 15, " +
                                                            "\"viewCount\": 123, " +
                                                            "\"imageUrls\": [\"https://image.../1.jpg\", \"https://image.../2.jpg\"], " +
                                                            "\"category\": { \"id\": 1, \"name\": \"상의\", \"depth\": 1 } " +
                                                            "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value =
                                                    "{ \"message\": \"리소스를 찾을 수 없습니다.\", \"details\": \"존재하지 않는 상품입니다.\" }"
                                    )
                            )
                    )
            }
    )
    public @interface FindProductDetail {}
}
