package dukku.product.boundedContext.product.app.cqrs;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dukku.common.shared.product.dto.cqrs.ProductSearchRequest;
import dukku.common.shared.product.dto.cqrs.ProductSortType;
import dukku.common.shared.product.dto.product.ProductListItemResponse;
import dukku.common.shared.product.dto.product.ProductListResponse;
import dukku.product.boundedContext.product.entity.query.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchProductUseCase {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductListResponse searchProducts(ProductSearchRequest request, Pageable pageable) {

        // 1. 쿼리 빌딩 (필터링 로직)
        Query boolQuery = Query.of(q -> q.bool(b -> {
            // (1) 키워드 검색 (제목, 설명)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                b.must(m -> m.multiMatch(mm -> mm
                        .fields("title^2", "description")
                        .query(request.getKeyword())));
            }

            // (2) 카테고리 검색 (계층형 - 족보 리스트 검색)
            if (request.getCategoryId() != null) {
                b.filter(f -> f.term(t -> t.field("categoryIds").value(request.getCategoryId())));
            }

            // (3) 가격 범위 검색
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                b.filter(f -> f.range(r -> r.number(n -> n
                        .field("price")
                        .gte(request.getMinPrice() != null ? request.getMinPrice().doubleValue() : null)
                        .lte(request.getMaxPrice() != null ? request.getMaxPrice().doubleValue() : null)
                )));
            }

            // (4) 상품 상태 검색 (미개봉, 사용감 있음 등)
            if (request.getConditionStatus() != null) {
                b.filter(f -> f.term(t -> t.field("conditionStatus").value(request.getConditionStatus().name())));
            }

            // (5) 태그 검색
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                List<FieldValue> tagValues = request.getTags().stream()
                        .map(FieldValue::of)
                        .toList();

                b.filter(f -> f
                        .terms(t -> t
                                .field("tags") // ProductDocument의 tags 필드
                                .terms(v -> v.value(tagValues))
                        )
                );
            }

            // (6) [핵심 로직] 판매 완료 상품 필터링 처리
            if (Boolean.TRUE.equals(request.getOnlySoldOut())) {
                // Case A: "판매 완료만 보기" 설정 ON
                // 무조건 SOLD_OUT 상태인 것만 필터링해서 가져옴
                b.filter(f -> f.term(t -> t.field("saleStatus").value("SOLD_OUT")));
            }
            // Case B: 설정 OFF (null or false) - 기본 동작
            // -> 별도의 필터를 걸지 않음. (모든 상태 조회)
            // -> 대신 아래 '정렬 로직'에 의해 '판매중(0)'이 먼저 나오고, '품절(1)'은 뒤페이지에 나옴.

            // (7) 필수: 노출 가능한 상품만 (삭제된 상품 제외)
            b.filter(f -> f.term(t -> t.field("visibilityStatus").value("VISIBLE")));

            return b;
        }));

        // 2. 정렬 설정 (다중 정렬)
        List<SortOptions> sortOptions = new ArrayList<>();

        // [1순위] 판매 상태 정렬 (판매중 우선)
        // onlySoldOut=true일 때: 전부 1이라서 의미 없음 (동등)
        // onlySoldOut=false일 때: 0(판매중) -> 1(품절) 순서로 정렬됨 (페이징 시 뒤로 밀림)
        sortOptions.add(SortOptions.of(s -> s
                .field(f -> f.field("saleSortPriority").order(SortOrder.Asc))));

        // [2순위] 사용자 선택 정렬 (최신순, 가격순, 인기순 등)
        sortOptions.add(getUserSortOption(request.getSortType()));

        // 3. 네이티브 쿼리 생성 및 실행
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .withSort(sortOptions)
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        // 4. 결과 변환
        List<ProductListItemResponse> items = searchHits.stream()
                .map(hit -> hit.getContent().toListItemResponse())
                .toList();

        return ProductListResponse.fromByQuery(new PageImpl<>(items, pageable, searchHits.getTotalHits()));
    }

    private SortOptions getUserSortOption(ProductSortType sortType) {
        if (sortType == null) sortType = ProductSortType.LATEST;
        String field = sortType.getField();

        // 가격 낮은순만 오름차순, 나머지는 내림차순(최신순, 좋아요순 등)
        SortOrder finalOrder = (sortType == ProductSortType.PRICE_LOW) ? SortOrder.Asc : SortOrder.Desc;
        return SortOptions.of(s -> s.field(f -> f.field(field).order(finalOrder)));
    }
}
