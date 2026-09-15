package com.venquet.aiops.crm.customer;

import com.venquet.aiops.crm.common.ConflictException;
import com.venquet.aiops.crm.common.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("A customer with this email already exists");
        }
        return CustomerResponse.from(customerRepository.save(new Customer(request.name(), request.email())));
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse findById(UUID id) {
        return CustomerResponse.from(findEntity(id));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        if (customerRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("A customer with this email already exists");
        }
        Customer customer = findEntity(id);
        customer.update(request.name(), request.email());
        return CustomerResponse.from(customer);
    }

    public Customer findEntity(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
