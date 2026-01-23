package dukku.semicolon.boundedContext.product.app.cqrs;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dukku.semicolon.boundedContext.product.entity.query.ProductDocument;
import dukku.semicolon.shared.product.dto.product.ProductListItemResponse;
import dukku.semicolon.shared.product.dto.product.ProductListResponse;
import dukku.semicolon.shared.product.dto.cqrs.ProductSearchRequest;
import dukku.semicolon.shared.product.dto.cqrs.ProductSortType;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchProductUseCase {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductListResponse searchProducts(ProductSearchRequest request, Pageable pageable) {

        // 1. 쿼리 (필터링)
        Query boolQuery = Query.of(q -> q.bool(b -> {
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                b.must(m -> m.multiMatch(mm -> mm
                        .fields("title^2", "description")
                        .query(request.getKeyword())));
            }
            if (request.getCategoryId() != null) {
                b.filter(f -> f.term(t -> t.field("categoryId").value(request.getCategoryId())));
            }
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                b.filter(f -> f.range(r -> r.number(n -> n
                        .field("price")
                        .gte(request.getMinPrice() != null ? request.getMinPrice().doubleValue() : null)
                        .lte(request.getMaxPrice() != null ? request.getMaxPrice().doubleValue() : null)
                )));
            }
            if (request.getConditionStatus() != null) {
                b.filter(f -> f.term(t -> t.field("conditionStatus").value(request.getConditionStatus().name())));
            }
            // 필수: 노출 가능한 상품만
            b.filter(f -> f.term(t -> t.field("visibilityStatus").value("VISIBLE")));
            return b;
        }));

        // 2. 정렬 (다중 정렬)
        List<SortOptions> sortOptions = new ArrayList<>();

        // [1순위] 품절 상품 내리기
        // SyncService에서 미리 계산해둔 saleSortPriority (0:판매중 -> 1:품절) 사용
        sortOptions.add(SortOptions.of(s -> s
                .field(f -> f.field("saleSortPriority").order(SortOrder.Asc))));

        // [2순위] 사용자 선택 (최신순 등)
        sortOptions.add(getUserSortOption(request.getSortType()));

        // 3. 실행
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .withSort(sortOptions)
                .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        // 4. 변환
        List<ProductListItemResponse> items = searchHits.stream()
                .map(hit -> ProductListItemResponse.from(hit.getContent()))
                .collect(Collectors.toList());

        return ProductListResponse.fromByQuery(new PageImpl<>(items, pageable, searchHits.getTotalHits()));
    }

    private SortOptions getUserSortOption(ProductSortType sortType) {
        if (sortType == null) sortType = ProductSortType.LATEST;
        String field = sortType.getField();

        SortOrder finalOrder = (sortType == ProductSortType.PRICE_LOW) ? SortOrder.Asc : SortOrder.Desc;
        return SortOptions.of(s -> s.field(f -> f.field(field).order(finalOrder)));
    }
}