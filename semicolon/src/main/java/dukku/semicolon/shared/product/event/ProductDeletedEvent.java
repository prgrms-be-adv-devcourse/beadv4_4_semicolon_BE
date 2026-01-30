package dukku.semicolon.shared.product.event;

import dukku.semicolon.boundedContext.product.entity.Product;

public record ProductDeletedEvent(Product product) {
}
