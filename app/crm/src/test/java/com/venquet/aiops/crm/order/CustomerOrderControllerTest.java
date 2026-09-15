package com.venquet.aiops.crm.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerOrderController.class)
class CustomerOrderControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CustomerOrderService orderService;

    @Test
    void createsOrder() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.create(any())).thenReturn(new CustomerOrderResponse(orderId, customerId, "ORD-1001",
                OrderStatus.PENDING, new BigDecimal("49.99"), "USD", Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"" + customerId + "\",\"orderNumber\":\"ORD-1001\",\"totalAmount\":49.99,\"currency\":\"USD\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
