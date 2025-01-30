package org.reactive.services;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;
import org.reactive.entities.Product;
import org.reactive.repositories.ProductRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    EventBus eventBus;

    private static final Logger LOG = Logger.getLogger(ProductServiceImpl.class);

    @Override
    @WithSession
    public Uni<List<Product>> listAllProducts() {
        return productRepository.findAll().list()
                .onFailure().recoverWithItem(List.of());
    }

    @Override
    @WithSession
    public Uni<Product> getProductById(Long id) {
        return productRepository.findById(id)
                .onItem().ifNull().failWith(new RuntimeException("Product not found"));
    }

    @Override
    @WithTransaction
    public Uni<Product> createProduct(Product product) {
        return productRepository.persist(product)
                .onFailure().recoverWithUni(Uni.createFrom().failure(new RuntimeException("Failed to create product")))
                .onItem().invoke(createdProduct -> {
                    JsonObject productJson = JsonObject.mapFrom(product);
                    LOG.info("Publishing product.created event with ID: " + createdProduct.getId());
                    eventBus.publish("product.created", productJson);
                });
    }

    @Override
    @WithTransaction
    public Uni<Boolean> deleteProduct(Long id) {
        return productRepository.deleteById(id)
                .onFailure().recoverWithUni(Uni.createFrom().failure(new RuntimeException("Failed to delete product")));
    }

    @Override
    @WithTransaction
    public Uni<Product> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .onFailure().recoverWithUni(Uni.createFrom().failure(new RuntimeException("Product not found")))
                .onItem().ifNotNull().transformToUni(existingProduct -> {
                    existingProduct.setCode(updatedProduct.getCode());
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    return productRepository.persist(existingProduct)
                            .onFailure().recoverWithUni(Uni.createFrom().failure(new RuntimeException("Failed to update product")))
                            .onItem().invoke(updatedProduct1 -> {
                                JsonObject productJson = JsonObject.mapFrom(existingProduct);
                                eventBus.publish("product.updated", productJson);
                            });
                });
    }
}