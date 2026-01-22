package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
