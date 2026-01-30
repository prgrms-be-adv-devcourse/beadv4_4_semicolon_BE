package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String name);
}
