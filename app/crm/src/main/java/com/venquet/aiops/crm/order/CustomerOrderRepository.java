package com.venquet.aiops.crm.order;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    boolean existsByOrderNumber(String orderNumber);
    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
