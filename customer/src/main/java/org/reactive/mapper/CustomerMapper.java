package org.reactive.mapper;

import org.reactive.dtos.CustomerDTO;
import org.reactive.entities.Customer;

public class CustomerMapper {
    public static CustomerDTO toDTO(Customer customer) {
        return new CustomerDTO(
                customer.getCode(),
                customer.getAccountNumber(),
                customer.getNames(),
                customer.getSurname()
        );
    }
}
