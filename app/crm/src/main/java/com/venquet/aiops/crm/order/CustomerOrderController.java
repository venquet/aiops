package com.venquet.aiops.crm.order;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<CustomerOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        CustomerOrderResponse order = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @GetMapping
    public List<CustomerOrderResponse> findAll() { return orderService.findAll(); }

    @GetMapping("/{id}")
    public CustomerOrderResponse findById(@PathVariable UUID id) { return orderService.findById(id); }
}
