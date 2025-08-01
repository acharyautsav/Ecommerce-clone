package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.*;
import com.Java.FinalProject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
                order.setProduct(product);
                order.setQuantity(item.getItemsOrderedQuantity());
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
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
    
    public void generateReceiptPDF(Order order, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"receipt_" + order.getOrderId() + ".pdf\"");
        
        // Create HTML content for the receipt
        String htmlContent = generateReceiptHTML(order);
        
        // Convert HTML to PDF using iText
        try {
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private String generateReceiptHTML(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' hh:mm a");
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Order Receipt</title>
                <style>
                    body { 
                        font-family: 'Courier New', monospace; 
                        margin: 40px; 
                        color: #333;
                        line-height: 1.6;
                    }
                                         .header { 
                         text-align: center; 
                         margin-bottom: 25px; 
                     }
                     .receipt-title { 
                         font-size: 22px; 
                         font-weight: bold; 
                         color: #333; 
                         margin-bottom: 8px;
                     }
                     .brand-name {
                         font-size: 18px;
                         font-weight: bold;
                         color: #333;
                         margin-bottom: 3px;
                     }
                     .tagline {
                         font-size: 12px;
                         color: #666;
                         margin-bottom: 15px;
                     }
                     .divider {
                         border: none;
                         border-top: 1px solid #ddd;
                         margin-bottom: 20px;
                     }
                                         .section {
                         margin-bottom: 20px;
                     }
                     .section-title {
                         font-weight: bold;
                         color: #333;
                         margin-bottom: 12px;
                         font-size: 16px;
                     }
                     .info-row {
                         margin-bottom: 6px;
                     }
                    .label {
                        font-weight: bold;
                        color: #333;
                    }
                    .value {
                        color: #666;
                        margin-left: 10px;
                    }
                    .status {
                        color: #28a745;
                        font-weight: bold;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 40px;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="receipt-title">Order Receipt</div>
                    <div class="brand-name">ShopMart</div>
                    <div class="tagline">Your Trusted Shopping Partner</div>
                </div>
                
                <hr class="divider">
                
                <div class="section">
                    <div class="info-row">
                        <span class="label">Order ID:</span>
                        <span class="value">#%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Date:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Status:</span>
                        <span class="value status">%s</span>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">Customer Information</div>
                    <div class="info-row">
                        <span class="label">Name:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Email:</span>
                        <span class="value">%s</span>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">Product Details</div>
                    <div class="info-row">
                        <span class="label">Product:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Quantity:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Unit Price:</span>
                        <span class="value">₹%.2f</span>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">Payment Information</div>
                    <div class="info-row">
                        <span class="label">Payment Method:</span>
                        <span class="value">%s</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Payment Status:</span>
                        <span class="value status">%s</span>
                    </div>
                </div>
                
                                 <div class="footer">
                     <p>This is an official receipt from ShopMart</p>
                     <p>Generated on %s</p>
                 </div>
                 
                 <div style="text-align: center; margin-top: 30px;">
                     <button style="background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 4px; margin-right: 10px; font-family: 'Courier New', monospace;">Close</button>
                     <button style="background: #007bff; color: white; border: none; padding: 8px 16px; border-radius: 4px; font-family: 'Courier New', monospace;">Save as PDF</button>
                 </div>
            </body>
            </html>
            """.formatted(
                order.getOrderId(),
                order.getCreatedAt().format(formatter),
                order.getOrderStatus(),
                order.getCustomer().getCustomerName(),
                order.getCustomer().getCustomerEmail(),
                order.getProduct().getProductName(),
                order.getQuantity(),
                order.getProduct().getPrice(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                java.time.LocalDateTime.now().format(formatter)
            );
    }
} 