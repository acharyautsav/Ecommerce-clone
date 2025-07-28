package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by seller
    List<Order> findBySeller_SellerIdOrderByCreatedAtDesc(Long sellerId);
    
    // Find orders by customer
    List<Order> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);
    
    // Find orders by payment status
    List<Order> findByPaymentStatus(String paymentStatus);
    
    // Find orders by order status
    List<Order> findByOrderStatus(String orderStatus);
    
    // Find orders by seller and order status
    List<Order> findBySeller_SellerIdAndOrderStatusOrderByCreatedAtDesc(Long sellerId, String orderStatus);
} 