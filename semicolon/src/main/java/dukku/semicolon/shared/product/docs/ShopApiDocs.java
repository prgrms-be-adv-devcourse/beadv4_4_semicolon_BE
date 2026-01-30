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

public final class ShopApiDocs {

    private ShopApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "상점 API", description = "상점 정보 조회 및 관리를 위한 API")
    public @interface ShopTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "내 상점 조회", description = "로그인한 사용자의 상점 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 상점 정보 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "shopUuid": "s1h2o3p4-e5f6-7890-1234-567890abcdef",
              "shopName": "내 상점",
              "description": "내 상점입니다. 많이 이용해주세요."
            }
            """)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    @ApiResponse(responseCode = "404", description = "상점을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상점을 찾을 수 없습니다.\"}")))
    public @interface FindMyShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "내 상점 소개 수정", description = "로그인한 사용자의 상점 소개를 수정합니다.")
    @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "description": "새로운 상점 소개입니다."
            }
            """)))
    @ApiResponse(responseCode = "200", description = "내 상점 소개 수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "shopUuid": "s1h2o3p4-e5f6-7890-1234-567890abcdef",
              "shopName": "내 상점",
              "description": "새로운 상점 소개입니다."
            }
            """)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    public @interface UpdateMyShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "내 상점 상품 목록 조회", description = "로그인한 사용자의 상점에 등록된 상품 목록을 조회합니다. 판매 상태(SaleStatus)로 필터링할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "내 상점 상품 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "products": [
                {
                  "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "productName": "내가 판매하는 상품",
                  "price": 50000,
                  "saleStatus": "ON_SALE"
                }
              ],
              "totalPages": 5,
              "totalElements": 50,
              "currentPage": 0
            }
            """)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"인증이 필요합니다.\"}")))
    public @interface FindMyShopProducts {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "판매자 상점 조회 (공개)", description = "특정 판매자의 공개된 상점 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "판매자 상점 정보 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "shopUuid": "s1h2o3p4-e5f6-7890-1234-abcdefghij",
              "shopName": "다른 사람의 상점",
              "description": "구경하고 가세요."
            }
            """)))
    @ApiResponse(responseCode = "404", description = "상점을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상점을 찾을 수 없습니다.\"}")))
    public @interface FindShop {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "판매자 상점 상품 목록 조회 (공개)", description = "특정 판매자의 상점에 등록된, 현재 판매중인 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "판매자 상점 상품 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "products": [
                {
                  "productUuid": "p1r2o3d4-e5f6-7890-1234-567890abcdef",
                  "productName": "다른 사람의 상품",
                  "price": 120000,
                  "imageUrl": "https://example.com/other_image.jpg"
                }
              ],
              "totalPages": 2,
              "totalElements": 20,
              "currentPage": 0
            }
            """)))
    @ApiResponse(responseCode = "404", description = "상점을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"상점을 찾을 수 없습니다.\"}")))
    public @interface FindShopProducts {}
}
