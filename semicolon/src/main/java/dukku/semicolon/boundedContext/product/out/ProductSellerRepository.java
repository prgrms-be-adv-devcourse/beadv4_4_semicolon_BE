package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.ProductSeller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductSellerRepository extends JpaRepository<ProductSeller, Integer> {
    Optional<ProductSeller> findByUserUuid(UUID userUuid);
}
