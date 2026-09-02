package com.example.productapi;

import java.math.BigDecimal;

public record Product(Long id, String name, BigDecimal price) {
    static Product from(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getPrice());
    }
}
