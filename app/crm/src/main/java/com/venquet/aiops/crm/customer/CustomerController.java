package com.venquet.aiops.crm.customer;

import com.venquet.aiops.crm.order.CustomerOrderResponse;
import com.venquet.aiops.crm.order.CustomerOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerOrderService orderService;

    public CustomerController(CustomerService customerService, CustomerOrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse customer = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/customers/" + customer.id())).body(customer);
    }

    @GetMapping
    public List<CustomerResponse> findAll() { return customerService.findAll(); }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable UUID id) { return customerService.findById(id); }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @GetMapping("/{id}/orders")
    public List<CustomerOrderResponse> findOrders(@PathVariable UUID id) {
        return orderService.findByCustomerId(id);
    }
}
