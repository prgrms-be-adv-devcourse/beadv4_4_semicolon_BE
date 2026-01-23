package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.ProductLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Integer> {

    boolean existsByUserUuidAndProduct_Uuid(UUID userUuid, UUID productUuid);

    Optional<ProductLike> findByUserUuidAndProduct_Uuid(UUID userUuid, UUID productUuid);

    long countByProduct_Uuid(UUID productUuid);

    @Query("""
        select pl.product.uuid
        from ProductLike pl
        where pl.userUuid = :userUuid
        order by pl.createdAt desc
    """)
    Page<UUID> findProductUuidsByUserUuid(UUID userUuid, Pageable pageable);
}
