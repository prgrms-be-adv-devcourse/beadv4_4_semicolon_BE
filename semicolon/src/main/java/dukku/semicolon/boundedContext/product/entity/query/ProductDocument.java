package dukku.semicolon.boundedContext.product.entity.query;

import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 1. 인덱스 이름과 설정 파일 경로 지정
@Document(indexName = "products_v1")
@Setting(settingPath = "elasticsearch/product-setting.json")
public class ProductDocument {
    // ES 내부 관리용 ID (MySQL의 PK값. 예: "1", "2")
    // 동기화 시 이 ID를 기준으로 덮어쓰기(Update)를 수행함
    @Id
    private String id;

    // 클라이언트 반환용 UUID (외부 노출용)
    @Field(type = FieldType.Keyword)
    private String productUuid;

    // --- 정렬 최적화 필드 ---
    // 0: 판매중/예약중, 1: 품절 (SyncService에서 계산해서 넣음)
    @Field(type = FieldType.Integer)
    private Integer saleSortPriority;

    // --- 검색 필드 (형태소 분석 적용) ---
    @Field(type = FieldType.Text, analyzer = "korean_analyzer", searchAnalyzer = "korean_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer", searchAnalyzer = "korean_analyzer")
    private String description;

    // --- 필터링 필드 (정확히 일치해야 함 -> Keyword) ---
    @Field(type = FieldType.Keyword)
    private String sellerUuid;

    @Field(type = FieldType.Integer)
    private Integer categoryId;

    @Field(type = FieldType.Keyword)
    private SaleStatus saleStatus;

    @Field(type = FieldType.Keyword)
    private VisibilityStatus visibilityStatus;

    // --- 정렬 및 범위 검색 필드 ---
    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Long)
    private Long shippingFee;

    @Field(type = FieldType.Integer)
    private int viewCount;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    // --- UI 노출용 (검색 X, 저장만 O) ---
    @Field(type = FieldType.Keyword, index = false)
    private String thumbnailImageUrl;

    public static ProductDocument from(Product product) {
        return ProductDocument.builder()
                .id(String.valueOf(product.getId()))
                .sellerUuid(product.getSellerUuid().toString())
                .categoryId(product.getCategory().getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .shippingFee(product.getShippingFee())
                .saleStatus(product.getSaleStatus())
                .visibilityStatus(product.getVisibilityStatus())
                .likeCount(product.getLikeCount()) // 초기값 0
                .createdAt(product.getCreatedAt())
                // 썸네일 찾기 (없으면 null)
                .thumbnailImageUrl(product.getImages().stream()
                        .filter(ProductImage::isThumbnail)
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(null))
                .build();
    }
}
