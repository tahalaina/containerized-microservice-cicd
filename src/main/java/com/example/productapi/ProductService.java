package com.example.productapi;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentHashMap<Long, Product> products = new ConcurrentHashMap<>();

    public ProductService() {
        create(new CreateProductRequest("Starter product", new java.math.BigDecimal("9.99")));
    }

    public List<Product> findAll() { return products.values().stream().sorted(java.util.Comparator.comparing(Product::id)).toList(); }
    public Product findById(Long id) { return java.util.Optional.ofNullable(products.get(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found")); }
    public Product create(CreateProductRequest request) {
        long id = sequence.incrementAndGet();
        Product product = new Product(id, request.name().trim(), request.price());
        products.put(id, product);
        return product;
    }
}
