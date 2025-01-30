package org.reactive.services;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.reactive.dtos.CustomerDTO;
import org.reactive.entities.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.reactive.entities.Product;
import org.reactive.mapper.CustomerMapper;
import org.reactive.repositories.CustomerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
public class CustomerServiceImpl implements CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    WebClient webClient;

    @Override
    @WithSession
    public Uni<List<CustomerDTO>> listAllCustomers() {
        return customerRepository.findAll().list()
                .onFailure().transform(failure -> new RuntimeException("Error listing customers", failure))
                .onItem().transform(customers -> customers.stream().map(CustomerMapper::toDTO).collect(Collectors.toList()));
    }

    @Override
    @WithSession
    public Uni<CustomerDTO> getCustomer(Long id) {
        return customerRepository.findById(id)
                .onFailure().transform(failure -> new RuntimeException("Error getting customer with id: " + id, failure))
                .onItem().ifNotNull().transform(CustomerMapper::toDTO);
    }

    @Override
    @WithTransaction
    public Uni<CustomerDTO> createCustomer(Customer customer) {
        return customerRepository.persist(customer)
                .onFailure().transform(failure -> new RuntimeException("Error creating customer", failure))
                .onItem().ifNotNull().transform(CustomerMapper::toDTO);
    }

    @Override
    @WithTransaction
    public Uni<CustomerDTO> updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .onFailure().transform(failure -> new RuntimeException("Error finding customer with id: " + id, failure))
                .onItem().ifNotNull().transformToUni(existingCustomer -> {
                    existingCustomer.setCode(updatedCustomer.getCode());
                    existingCustomer.setAccountNumber(updatedCustomer.getAccountNumber());
                    existingCustomer.setNames(updatedCustomer.getNames());
                    existingCustomer.setSurname(updatedCustomer.getSurname());
                    return customerRepository.persist(existingCustomer).onItem().ifNotNull().transform(CustomerMapper::toDTO)
                            .onFailure().transform(failure -> new RuntimeException("Error updating customer with id: " + id, failure));
                });
    }

    @Override
    @WithTransaction
    public Uni<Boolean> deleteCustomer(Long id) {
        return customerRepository.deleteById(id)
                .onFailure().transform(failure -> new RuntimeException("Error deleting customer with id: " + id, failure));
    }


    @Override
    @WithTransaction
    public Uni<Customer> addProductToCustomer(Long customerId, Long productId) {
        return checkProductExists(productId)
                .onFailure().transform(failure -> new RuntimeException("Error adding product to customer", failure))
                .onItem().transformToUni(productExist -> fetchCustomerWithProducts(customerId)
                        .onFailure().transform(failure -> new RuntimeException("Error finding customer with id: " + customerId, failure))
                        .onItem().ifNotNull().transformToUni(customer -> {
                            Product product = new Product();
                            product.setProductId(productId);
                            product.setCustomer(customer);
                            customer.getProducts().add(product);
                            return customerRepository.persist(customer)
                                    .onFailure().transform(failure -> new RuntimeException("Error adding product to customer", failure));
                        })
                        .onItem().ifNull().failWith(new RuntimeException("Customer with id: " + customerId + " not found"))
                );
    }

    @Override
    public Uni<Customer> getCustomerWithProducts(Long customerId) {
        return fetchCustomerWithProducts(customerId)
                .onFailure().transform(failure -> new RuntimeException("Error with customer id: " + customerId, failure))
                .onItem().transformToUni(customer -> getAllProducts()
                        .onFailure().transform(failure -> new RuntimeException("Error retrieving products", failure))
                        .onItem().transform(productsFromService -> {
                            customer.getProducts().forEach(product ->
                                    productsFromService.stream()
                                            .filter(p -> product.getProductId().equals(p.getId()))
                                            .findFirst()
                                            .ifPresent(p -> {
                                                product.setCode(p.getCode());
                                                product.setName(p.getName());
                                                product.setDescription(p.getDescription());
                                            })
                            );
                            return customer;
                        })
                );

    }

    @WithSession
    public Uni<Customer> fetchCustomerWithProducts(Long customerId) {
        return customerRepository.findById(customerId)
                .onFailure().transform(failure -> new RuntimeException("Error finding customer with id: " + customerId, failure))
                .onItem().ifNotNull().transformToUni(customer ->
                        Mutiny.fetch(customer.getProducts()).replaceWith(customer)
                );
    }

    private Uni<List<Product>> getAllProducts() {
        return webClient.get(8081, "localhost", "/api/v1/products").send()
                .onFailure().transform(failure -> new RuntimeException("Error retrieving products", failure))
                .map(response -> {
                    if (response == null || response.statusCode() != 200) {
                        throw new RuntimeException("No products retrieved or response was not successful");
                    }
                    JsonArray products = response.bodyAsJsonArray();
                    List<Product> productList = new ArrayList<>();
                    products.forEach(product -> {
                        JsonObject productJson = (JsonObject) product;
                        Product p = new Product();
                        p.setId(productJson.getLong("id"));
                        p.setCode(productJson.getString("code"));
                        p.setName(productJson.getString("name"));
                        p.setDescription(productJson.getString("description"));
                        productList.add(p);
                    });
                    return productList;
                });
    }

    private Uni<Boolean> checkProductExists(Long productId) {
        return webClient.get(8081, "localhost", "/api/v1/products/" + productId).send()
                .onFailure().transform(failure -> new RuntimeException("Error retrieving product with id: " + productId, failure))
                .map(response -> {
                    if (response == null || response.statusCode() != 200) {
                        throw new RuntimeException("No product retrieved or response was not successful");
                    }
                    return true;
                });
    }
}