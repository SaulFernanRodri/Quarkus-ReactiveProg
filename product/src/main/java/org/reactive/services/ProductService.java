package org.reactive.services;

import io.smallrye.mutiny.Uni;
import org.reactive.entities.Product;

import java.util.List;

public interface ProductService {
    Uni<List<Product>> listAllProducts();

    Uni<Product> getProductById(Long id);

    Uni<Product> createProduct(Product product);

    Uni<Boolean> deleteProduct(Long id);

    Uni<Product> updateProduct(Long id, Product updatedProduct);
}
