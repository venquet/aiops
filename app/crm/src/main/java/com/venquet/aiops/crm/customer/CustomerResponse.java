package com.venquet.aiops.crm.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, String name, String email, Instant createdAt, Instant updatedAt) {
    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
