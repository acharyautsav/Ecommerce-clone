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
        Long sellerId = (Long) session.getAttribute("sellerId");
        
        if (sellerId == null) {
            return "redirect:/seller/login";
        }
        
        // Get all orders for this seller
        List<Order> allOrders = orderService.getOrdersBySeller(sellerId);
        List<Order> pendingOrders = orderService.getPendingOrdersBySeller(sellerId);
        
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        
        return "seller/orders";
    }
    
    @PostMapping("/seller/orders/confirm/{orderId}")
    @ResponseBody
    public String confirmOrder(@PathVariable Long orderId, HttpSession session) {
        Long sellerId = (Long) session.getAttribute("sellerId");
        
        if (sellerId == null) {
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
        Long sellerId = (Long) session.getAttribute("sellerId");
        
        if (sellerId == null) {
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
        Long sellerId = (Long) session.getAttribute("sellerId");
        
        if (sellerId == null) {
            return "error: Not logged in";
        }
        
        try {
            orderService.deliverOrder(orderId);
            return "success: Order delivered";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
} 