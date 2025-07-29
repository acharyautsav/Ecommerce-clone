package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.*;
import com.Java.FinalProject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    
    @Autowired
    private ItemsOrderedRepository itemsOrderedRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SellerRepository sellerRepository;
    
    @Autowired
    private CartService cartService;
    
    @Transactional
    public void processPaymentSuccess(Long customerId, String paymentMethod) {
        System.out.println("=== Processing Payment Success ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Payment Method: " + paymentMethod);
        
        // Get customer
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            System.out.println("ERROR: Customer not found!");
            return;
        }
        System.out.println("Customer found: " + customer.getCustomerName());
        
        // Get cart items using CartService
        List<ItemsOrdered> cartItems = cartService.getCartItemsByCustomerId(customerId);
        System.out.println("Cart items count: " + cartItems.size());
        
        if (cartItems.isEmpty()) {
            System.out.println("WARNING: Cart is empty, nothing to process");
            return;
        }
        
        // Group items by seller and create orders
        for (ItemsOrdered item : cartItems) {
            Product product = item.getProduct();
            Seller seller = sellerRepository.findById(product.getSellerId()).orElse(null);
            
            if (seller != null) {
                // Create new order for this seller
                Order order = new Order();
                order.setCustomer(customer);
                order.setSeller(seller);
                order.setTotalAmount(item.getItemsOrderedPrice() * item.getItemsOrderedQuantity());
                order.setPaymentStatus("PAID");
                order.setOrderStatus("PENDING");
                order.setPaymentMethod(paymentMethod);
                
                orderRepository.save(order);
                System.out.println("Created order: " + order.getOrderId() + " for seller: " + seller.getSellerName());
            } else {
                System.out.println("ERROR: Seller not found for product: " + product.getProductId());
            }
        }
        
        // Clear the cart using CartService
        System.out.println("Clearing cart using CartService...");
        cartService.clearCartByCustomerId(customerId);
        System.out.println("Cart cleared successfully!");
        
        System.out.println("=== Payment Processing Complete ===");
    }
    
    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySeller_SellerIdOrderByCreatedAtDesc(sellerId);
    }
    
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);
    }
    
    public List<Order> getPendingOrdersBySeller(Long sellerId) {
        return orderRepository.findBySeller_SellerIdAndOrderStatusOrderByCreatedAtDesc(sellerId, "PENDING");
    }
    
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setOrderStatus("CONFIRMED");
            order.setUpdatedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setOrderStatus("SHIPPED");
            order.setUpdatedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    
    @Transactional
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setOrderStatus("DELIVERED");
            order.setUpdatedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    
    @Transactional
    public void confirmAndShipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            // Update order status directly to SHIPPED (skipping CONFIRMED)
            order.setOrderStatus("SHIPPED");
            order.setUpdatedAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
            System.out.println("Order " + orderId + " confirmed and shipped successfully");
        } else {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
    }
} 