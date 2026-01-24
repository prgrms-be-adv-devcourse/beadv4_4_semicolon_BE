package dukku.semicolon.shared.product.docs;

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

public final class SellerProductApiDocs {

    private SellerProductApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "판매자 상품 API", description = "인증된 판매자의 상품 등록, 수정, 삭제를 위한 API")
    public @interface SellerProductTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록합니다.")
    @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "name": "새 상품",
              "description": "아주 좋은 새 상품입니다.",
              "price": 25000,
              "stock": 50,
              "categoryId": 1
            }
            """)))
    @ApiResponse(responseCode = "200", description = "상품 등록 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
              "productName": "새 상품",
              "description": "아주 좋은 새 상품입니다.",
              "price": 25000,
              "stock": 50,
              "images": [],
              "sellerInfo": {
                "shopUuid": "s1h2o3p4-e5f6-7890-1234-567890abcdef",
                "shopName": "판매자의 상점"
              }
            }
            """)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상품 이름은 필수입니다.\"}")))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    public @interface CreateProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 수정", description = "판매자가 등록한 상품의 정보를 수정합니다.")
    @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "name": "수정된 상품",
              "description": "더 좋아진 상품입니다.",
              "price": 30000,
              "stock": 40
            }
            """)))
    @ApiResponse(responseCode = "200", description = "상품 수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
              "productName": "수정된 상품",
              "description": "더 좋아진 상품입니다.",
              "price": 30000,
              "stock": 40,
              "images": [],
              "sellerInfo": {
                "shopUuid": "s1h2o3p4-e5f6-7890-1234-567890abcdef",
                "shopName": "판매자의 상점"
              }
            }
            """)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 수정할 권한이 없습니다.\"}")))
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 찾을 수 없습니다.\"}")))
    public @interface UpdateProduct {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 삭제", description = "판매자가 등록한 상품을 삭제합니다. 성공 시 204 No Content를 반환합니다.")
    @ApiResponse(responseCode = "204", description = "상품 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 삭제할 권한이 없습니다.\"}")))
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 찾을 수 없습니다.\"}")))
    public @interface DeleteProduct {}
}
