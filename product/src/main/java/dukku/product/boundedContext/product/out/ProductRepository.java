package dukku.product.boundedContext.product.out;

import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.product.boundedContext.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Integer>, CustomProductRepository {
    @Query("""
            select p.id
            from Product p
            where p.uuid = :productUuid
              and p.deletedAt is null
            """)
    Optional<Integer> findIdByUuidAndDeletedAtIsNull(UUID productUuid);

    @Query("""
            select p.id
            from Product p
            where p.deletedAt is null
            order by p.id asc
            """)
    Page<Integer> findActiveProductIds(Pageable pageable);

    @Query("""
            select distinct p
            from Product p
            left join fetch p.images i
            left join fetch p.category c
            where p.uuid = :uuid
            """)
    Optional<Product> findByUuidWithImagesAndCategory(@Param("uuid") UUID uuid);

    @Query("""
            select distinct p
            from Product p
            left join fetch p.images i
            left join fetch p.category c
            where p.id = :id
            """)
    Optional<Product> findByIdWithImagesAndCategory(@Param("id") Integer id);

    @Query("""
            select pt.id
            from ProductTag pt
            join pt.tag t
            where pt.product.id = :productId
            """)
    List<Long> preloadProductTagsByProductId(@Param("productId") Integer productId);

    Optional<Product> findByUuid(UUID productUuid);

    Optional<Product> findByUuidAndDeletedAtIsNull(UUID productUuid);

    boolean existsBySellerUuidAndCategory_IdAndTitleAndPriceAndDeletedAtIsNull(
            UUID sellerUuid, Integer categoryId, String title, Long price);
    boolean existsByCategory_IdAndTitleAndPriceAndDeletedAtIsNull(
            Integer categoryId, String title, Long price);

    List<Product> findAllByUuidIn(List<UUID> uuids);

    @EntityGraph(attributePaths = "images")
    List<Product> findByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    @EntityGraph(attributePaths = "images")
    Page<Product> findBySellerUuidAndDeletedAtIsNull(UUID sellerUuid, Pageable pageable);

    @EntityGraph(attributePaths = "images")
    Page<Product> findBySellerUuidAndSaleStatusAndDeletedAtIsNull(
            UUID sellerUuid, SaleStatus saleStatus, Pageable pageable);

    // 1. 카테고리별 조회 (Fallback용) - 인덱스 필수! (category_id)
    Page<Product> findByCategory_IdInAndVisibilityStatusAndDeletedAtIsNull(
            List<Integer> categoryIds,
            VisibilityStatus visibilityStatus,
            Pageable pageable);

    // 2. 전체 최신순 조회 (Fallback용)
    // 검색어가 들어와도 그냥 이걸로 돌려버립니다.
    Page<Product> findByVisibilityStatusAndDeletedAtIsNull(
            VisibilityStatus visibilityStatus,
            Pageable pageable);
}
