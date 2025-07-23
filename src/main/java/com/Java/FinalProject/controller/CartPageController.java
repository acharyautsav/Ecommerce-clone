package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CartService;
import com.Java.FinalProject.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartPageController {
    private final CartService cartService;
    private final CustomerService customerService;
    public CartPageController(CartService cartService, CustomerService customerService) {
        this.cartService = cartService;
        this.customerService = customerService;
    }
    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) return "redirect:/";
        Customer customer = customerService.findById(customerId).orElse(null);
        if (customer == null) return "redirect:/";
        model.addAttribute("customerName", customer.getCustomerName());
        var cartItems = cartService.getCartItems(customer);
        model.addAttribute("cartItems", cartItems);
        double cartTotal = cartItems.stream()
            .mapToDouble(i -> i.getItemsOrderedPrice() * i.getItemsOrderedQuantity())
            .sum();
        model.addAttribute("cartTotal", cartTotal);
        return "cart";
    }
} 