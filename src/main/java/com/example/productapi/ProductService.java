package com.example.productapi;

import java.util.List;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final ProductRepository products;
    public ProductService(ProductRepository products) { this.products = products; }
    @Transactional(readOnly = true)
    public List<Product> findAll() { return products.findAllByOrderByIdAsc().stream().map(Product::from).toList(); }
    @Transactional(readOnly = true)
    public Product findById(Long id) { return products.findById(id).map(Product::from).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found")); }
    @Transactional
    public Product create(CreateProductRequest request) {
        return Product.from(products.save(new ProductEntity(request.name().trim(), request.price())));
    }
}
