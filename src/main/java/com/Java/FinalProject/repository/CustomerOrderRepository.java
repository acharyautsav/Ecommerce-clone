package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.CustomerOrder;
import com.Java.FinalProject.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByCustomerAndOrderStatus(Customer customer, String orderStatus);
    List<CustomerOrder> findByCustomer(Customer customer);
    void deleteByCustomer(Customer customer);
} 