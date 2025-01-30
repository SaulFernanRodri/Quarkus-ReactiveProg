package org.reactive.services;

import io.smallrye.mutiny.Uni;
import org.reactive.dtos.CustomerDTO;
import org.reactive.entities.Customer;

import java.util.List;

public interface CustomerService {
    public Uni<List<CustomerDTO>> listAllCustomers();
    public Uni<CustomerDTO> getCustomer(Long id);
    public Uni<CustomerDTO> createCustomer(Customer customer);
    public Uni<Boolean> deleteCustomer(Long id);
    public Uni<CustomerDTO> updateCustomer(Long id,Customer updatedCustomer);
    public Uni<Customer> addProductToCustomer(Long customerId, Long productId);
    public Uni<Customer> getCustomerWithProducts(Long customerId);
}
