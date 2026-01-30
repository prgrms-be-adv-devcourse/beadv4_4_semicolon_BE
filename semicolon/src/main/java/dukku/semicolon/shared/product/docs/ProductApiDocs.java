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

public final class ProductApiDocs {

    private ProductApiDocs() {}

    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(name = "상품 API", description = "상품 조회 및 관리를 위한 API")
    public @interface ProductTag {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "카테고리 목록 조회", description = "모든 상품 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            [
              {
                "id": 1,
                "name": "전자기기"
              },
              {
                "id": 2,
                "name": "의류"
              }
            ]
            """)))
    public @interface FindCategories {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "추천 상품 목록 조회", description = "추천 상품 목록을 조회합니다. (예: 최신순, 인기순 등)")
    @ApiResponse(responseCode = "200", description = "추천 상품 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            [
              {
                "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                "productName": "최신형 노트북",
                "price": 1500000,
                "imageUrl": "https://example.com/image.jpg"
              }
            ]
            """)))
    public @interface FindFeaturedProducts {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 목록 조회", description = "카테고리, 정렬 기준, 페이징을 통해 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "products": [
                {
                  "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "productName": "최신형 노트북",
                  "price": 1500000,
                  "imageUrl": "https://example.com/image.jpg"
                }
              ],
              "totalPages": 10,
              "totalElements": 100,
              "currentPage": 0
            }
            """)))
    public @interface FindProductList {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
              "productName": "최신형 노트북",
              "description": "최고의 성능을 자랑하는 노트북입니다.",
              "price": 1500000,
              "stock": 10,
              "images": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
              "sellerInfo": {
                "shopUuid": "s1h2o3p4-e5f6-7890-1234-567890abcdef",
                "shopName": "최고의 상점"
              }
            }
            """)))
    @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"해당 상품을 찾을 수 없습니다.\"}")))
    public @interface FindProductDetail {}

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(summary = "상품 재고 예약 (내부용)", description = "내부 서비스 간 통신을 통해 주문에 필요한 상품 재고를 예약합니다. 클라이언트 또는 프론트엔드에서 직접 호출하지 않습니다.")
    @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "orderItems": [
                {
                  "productUuid": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "quantity": 1
                }
              ]
            }
            """)))
    @ApiResponse(responseCode = "200", description = "상품 재고 예약 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 재고 부족)", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"재고가 부족합니다.\"}")))
    public @interface ReserveProducts {}
}
