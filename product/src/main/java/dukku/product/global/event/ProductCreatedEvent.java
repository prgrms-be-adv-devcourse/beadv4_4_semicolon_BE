package dukku.product.global.event;

import dukku.product.boundedContext.product.entity.Product;

public record ProductCreatedEvent(Product product) {
}
