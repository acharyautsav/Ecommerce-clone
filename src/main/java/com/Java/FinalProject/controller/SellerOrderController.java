package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Order;
import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class SellerOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/seller/orders")
    public String sellerOrders(HttpSession session, Model model) {
        // Get seller from session
        Seller seller = (Seller) session.getAttribute("seller");
        
        if (seller == null) {
            return "redirect:/seller/login";
        }
        
        // Get all orders for this seller
        List<Order> allOrders = orderService.getOrdersBySeller(seller.getSellerId());
        List<Order> pendingOrders = orderService.getPendingOrdersBySeller(seller.getSellerId());
        
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        
        return "seller/orders";
    }
    
    @PostMapping("/seller/orders/confirm/{orderId}")
    @ResponseBody
    public String confirmOrder(@PathVariable Long orderId, HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");
        
        if (seller == null) {
            return "error: Not logged in";
        }
        
        try {
            orderService.confirmOrder(orderId);
            return "success: Order confirmed";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/seller/orders/ship/{orderId}")
    @ResponseBody
    public String shipOrder(@PathVariable Long orderId, HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");
        
        if (seller == null) {
            return "error: Not logged in";
        }
        
        try {
            orderService.shipOrder(orderId);
            return "success: Order shipped";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/seller/orders/deliver/{orderId}")
    @ResponseBody
    public String deliverOrder(@PathVariable Long orderId, HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");
        
        if (seller == null) {
            return "error: Not logged in";
        }
        
        try {
            orderService.deliverOrder(orderId);
            return "success: Order delivered";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/seller/orders/confirm-and-ship/{orderId}")
    @ResponseBody
    public String confirmAndShipOrder(@PathVariable Long orderId, HttpSession session) {
        System.out.println("=== Confirm and Ship Order Request ===");
        System.out.println("Order ID: " + orderId);
        
        Seller seller = (Seller) session.getAttribute("seller");
        System.out.println("Seller from session: " + (seller != null ? seller.getSellerName() : "NULL"));
        
        if (seller == null) {
            System.out.println("ERROR: Seller not found in session");
            return "error: Not logged in";
        }
        
        try {
            orderService.confirmAndShipOrder(orderId);
            System.out.println("Order " + orderId + " confirmed and shipped successfully");
            return "success: Order confirmed and shipped";
        } catch (Exception e) {
            System.err.println("Error confirming and shipping order: " + e.getMessage());
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
} 