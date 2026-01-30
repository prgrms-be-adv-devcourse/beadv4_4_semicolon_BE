package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.boundedContext.product.entity.query.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
}
