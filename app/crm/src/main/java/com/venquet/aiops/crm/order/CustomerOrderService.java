package com.venquet.aiops.crm.order;

import com.venquet.aiops.crm.common.ConflictException;
import com.venquet.aiops.crm.common.ResourceNotFoundException;
import com.venquet.aiops.crm.customer.Customer;
import com.venquet.aiops.crm.customer.CustomerService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerOrderService {
    private final CustomerOrderRepository orderRepository;
    private final CustomerService customerService;

    public CustomerOrderService(CustomerOrderRepository orderRepository, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
    }

    @Transactional
    public CustomerOrderResponse create(CreateOrderRequest request) {
        if (orderRepository.existsByOrderNumber(request.orderNumber())) {
            throw new ConflictException("An order with this order number already exists");
        }
        Customer customer = customerService.findEntity(request.customerId());
        CustomerOrder order = new CustomerOrder(customer, request.orderNumber(), request.totalAmount(), request.currency());
        return CustomerOrderResponse.from(orderRepository.save(order));
    }

    public CustomerOrderResponse findById(UUID id) {
        return CustomerOrderResponse.from(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id)));
    }

    public List<CustomerOrderResponse> findAll() {
        return orderRepository.findAll().stream().map(CustomerOrderResponse::from).toList();
    }

    public List<CustomerOrderResponse> findByCustomerId(UUID customerId) {
        customerService.findEntity(customerId);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(CustomerOrderResponse::from).toList();
    }
}
