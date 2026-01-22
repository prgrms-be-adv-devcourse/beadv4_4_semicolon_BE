package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByUuid(UUID productUuid);
}
