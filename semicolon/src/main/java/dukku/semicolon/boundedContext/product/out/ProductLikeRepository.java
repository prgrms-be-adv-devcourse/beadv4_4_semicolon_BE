package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.ProductLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Integer> {

    boolean existsByUserUuidAndProduct_Uuid(UUID userUuid, UUID productUuid);

    Optional<ProductLike> findByUserUuidAndProduct_Uuid(UUID userUuid, UUID productUuid);

    long countByProduct_Uuid(UUID productUuid);

    Page<ProductLike> findByUserUuid(UUID userUuid, Pageable pageable);
}
