package com.venquet.aiops.crm.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotBlank @Size(max = 40) String orderNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal totalAmount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {
}
