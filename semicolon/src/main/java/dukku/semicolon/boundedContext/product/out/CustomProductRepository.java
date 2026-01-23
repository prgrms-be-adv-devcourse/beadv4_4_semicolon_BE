package dukku.semicolon.boundedContext.product.out;

import dukku.semicolon.shared.product.dto.cqrs.ProductStatDto;

import java.util.List;

public interface CustomProductRepository {
    void bulkUpdateProductStats(List<ProductStatDto> stats);
}
