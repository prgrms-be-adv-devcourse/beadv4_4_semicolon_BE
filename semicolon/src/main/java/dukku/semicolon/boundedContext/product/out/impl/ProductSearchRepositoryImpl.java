package dukku.semicolon.boundedContext.product.out.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.QProduct;
import dukku.semicolon.boundedContext.product.out.ProductSearchRepository;
import dukku.semicolon.shared.product.dto.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductSearchCondition cond, Pageable pageable) {
        QProduct p = QProduct.product;

        BooleanBuilder where = buildWhere(cond, p);
        OrderSpecifier<?> order = toOrder(cond, p);

        // 1) total count
        Long total = queryFactory
                .select(p.count())
                .from(p)
                .where(where)
                .fetchOne();

        long totalCount = (total == null) ? 0 : total;
        if (totalCount == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2) page ids (id만 페이징)
        List<Integer> ids = queryFactory
                .select(p.id)
                .from(p)
                .where(where)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, totalCount);
        }

        // 3) fetch products with images in one shot
        // distinct 안 하면 images 때문에 중복 row가 생길 수 있음
        List<Product> fetched = queryFactory
                .selectFrom(p)
                .distinct()
                .leftJoin(p.images).fetchJoin()
                .where(p.id.in(ids))
                .fetch();

        // 4) 원래 ids 순서대로 정렬(쿼리 IN절은 순서 보장 안 함)
        Map<Integer, Product> map = fetched.stream()
                .collect(Collectors.toMap(Product::getId, it -> it, (a, b) -> a));

        List<Product> content = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanBuilder buildWhere(ProductSearchCondition cond, QProduct p) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(p.deletedAt.isNull());
        where.and(p.visibilityStatus.eq(VisibilityStatus.VISIBLE));

        if (cond.getKeyword() != null && !cond.getKeyword().isBlank()) {
            String kw = cond.getKeyword().trim();
            where.and(
                    p.title.containsIgnoreCase(kw)
                            .or(p.description.containsIgnoreCase(kw))
            );
        }
        if (cond.getCategoryId() != null) {
            where.and(p.category.id.eq(cond.getCategoryId()));
        }
        if (cond.getSaleStatus() != null) {
            where.and(p.saleStatus.eq(cond.getSaleStatus()));
        }
        if (cond.getMinPrice() != null) {
            where.and(p.price.goe(cond.getMinPrice()));
        }
        if (cond.getMaxPrice() != null) {
            where.and(p.price.loe(cond.getMaxPrice()));
        }
        return where;
    }

    private OrderSpecifier<?> toOrder(ProductSearchCondition cond, QProduct p) {
        ProductSearchCondition.ProductSort sort =
                (cond.getSort() == null) ? ProductSearchCondition.ProductSort.LATEST : cond.getSort();

        return switch (sort) {
            case PRICE_ASC -> p.price.asc().nullsLast();
            case PRICE_DESC -> p.price.desc().nullsLast();
            case LIKE_DESC -> p.likeCount.desc();
            case LATEST -> p.createdAt.desc();
        };
    }
}
