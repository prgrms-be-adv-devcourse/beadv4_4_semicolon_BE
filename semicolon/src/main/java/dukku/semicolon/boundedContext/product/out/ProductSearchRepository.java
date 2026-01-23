package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.shared.product.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository {
    Page<Product> search(ProductSearchCondition cond, Pageable pageable);
}
