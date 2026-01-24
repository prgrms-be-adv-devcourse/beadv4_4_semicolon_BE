package dukku.semicolon.shared.product.event;

import dukku.semicolon.shared.product.dto.cqrs.ProductStatDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductStatsBulkUpdatedEvent {
    private List<ProductStatDto> stats;
}
