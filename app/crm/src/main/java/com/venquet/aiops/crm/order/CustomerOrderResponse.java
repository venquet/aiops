package com.venquet.aiops.crm.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerOrderResponse(UUID id, UUID customerId, String orderNumber, OrderStatus status,
                                    BigDecimal totalAmount, String currency, Instant createdAt, Instant updatedAt) {
    static CustomerOrderResponse from(CustomerOrder order) {
        return new CustomerOrderResponse(order.getId(), order.getCustomer().getId(), order.getOrderNumber(),
                order.getStatus(), order.getTotalAmount(), order.getCurrency(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
