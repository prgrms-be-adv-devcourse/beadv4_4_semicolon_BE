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

public final class ProductLikeApiDocs {

    private ProductLikeApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "상품 좋아요 API", description = "상품 좋아요/취소 및 목록 조회를 위한 API")
    public @interface ProductLikeTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 좋아요", description = "특정 상품에 '좋아요'를 표시합니다. 성공 시 200 OK를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "상품 좋아요 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 찾을 수 없습니다.\"}")))
    public @interface LikeProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 좋아요 취소", description = "특정 상품의 '좋아요'를 취소합니다. 성공 시 200 OK를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "상품 좋아요 취소 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    @ApiResponse(responseCode = "404", description = "상품 또는 좋아요를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"좋아요한 상품을 찾을 수 없습니다.\"}")))
    public @interface UnlikeProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "내가 좋아요한 상품 목록 조회", description = "현재 로그인한 사용자가 '좋아요'한 상품 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내가 좋아요한 상품 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "products": [
                {
                  "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "productName": "좋아요한 노트북",
                  "price": 1500000,
                  "imageUrl": "https://example.com/liked_image.jpg"
                }
              ],
              "totalPages": 1,
              "totalElements": 1,
              "currentPage": 0
            }
            """)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    public @interface MyLikes {}
}
