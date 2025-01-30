package org.reactive.dtos;

public record CustomerDTO(
        String code,
        String accountNumber,
        String names,
        String surname
) {}

