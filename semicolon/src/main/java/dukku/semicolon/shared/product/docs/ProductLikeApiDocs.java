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

public final class ProductLikeApiDocs {

    private ProductLikeApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "상품 좋아요(찜) API",
            description = "상품 좋아요/좋아요 취소 및 내 좋아요 목록 조회"
    )
    public @interface ProductLikeTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "상품 좋아요(찜) 등록",
            description = "상품에 좋아요(찜)를 등록합니다. 이미 좋아요 상태면 그대로 유지됩니다.",
            parameters = {
                    @Parameter(name = "productUuid", description = "상품 UUID", required = true,
                            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                    @Parameter(name = "X-USER-UUID", description = "임시 사용자 UUID 헤더", required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Like Product",
                                            value = """
                                                    {
                                                      "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                      "liked": true,
                                                      "likeCount": 151
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음",
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
    public @interface LikeProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "상품 좋아요(찜) 취소",
            description = "상품 좋아요(찜)를 취소합니다. 좋아요 상태가 아니어도 정상 처리됩니다.",
            parameters = {
                    @Parameter(name = "productUuid", description = "상품 UUID", required = true,
                            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                    @Parameter(name = "X-USER-UUID", description = "임시 사용자 UUID 헤더", required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 취소 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Unlike Product",
                                            value = """
                                                    {
                                                      "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                      "liked": false,
                                                      "likeCount": 150
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "상품을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "리소소스를 찾을 수 없습니다.",
                                                      "details": "존재하지 않는 상품입니다."
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface UnlikeProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "내 좋아요(찜) 목록 조회",
            description = "내가 좋아요한 상품 목록을 페이징으로 조회합니다.",
            parameters = {
                    @Parameter(name = "X-USER-UUID", description = "임시 사용자 UUID 헤더", required = true,
                            example = "7fa85f64-5717-4562-b3fc-2c963f66afa6"),
                    @Parameter(name = "page", description = "페이지(0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지 사이즈(최대 50)", example = "20")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 좋아요 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "My Liked Products",
                                            value = """
                                                    {
                                                      "items": [
                                                        {
                                                          "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                          "title": "오프화이트 후드",
                                                          "price": 120000,
                                                          "thumbnailUrl": "https://image.../1.jpg",
                                                          "likeCount": 15
                                                        }
                                                      ],
                                                      "page": 0,
                                                      "size": 20,
                                                      "totalCount": 42,
                                                      "hasNext": true
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public @interface FindMyLikes {}
}
