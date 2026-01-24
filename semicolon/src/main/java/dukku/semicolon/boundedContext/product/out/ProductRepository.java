package dukku.semicolon.boundedContext.product.out;

import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByUuid(UUID productUuid);

    Optional<Product> findByUuidAndDeletedAtIsNull(UUID productUuid);

    boolean existsByUuidAndDeletedAtIsNull(UUID productUuid);

    @EntityGraph(attributePaths = "images")
    List<Product> findByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    @EntityGraph(attributePaths = "images")
    Page<Product> findBySellerUuidAndDeletedAtIsNull(UUID sellerUuid, Pageable pageable);

    @EntityGraph(attributePaths = "images")
    Page<Product> findBySellerUuidAndSaleStatusAndDeletedAtIsNull(
            UUID sellerUuid, SaleStatus saleStatus, Pageable pageable
    );

    // 목록: visibility=VISIBLE, deletedAt=null 기본
    Page<Product> findByVisibilityStatusAndDeletedAtIsNull(
            VisibilityStatus visibilityStatus,
            Pageable pageable
    );

    // 카테고리 필터 목록
    Page<Product> findByCategory_IdAndVisibilityStatusAndDeletedAtIsNull(
            Integer categoryId,
            VisibilityStatus visibilityStatus,
            Pageable pageable
    );
}
