package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.entity.ItemsOrdered;
import com.Java.FinalProject.service.CartService;
import com.Java.FinalProject.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final CustomerService customerService;

    // Helper to get logged-in customer from session
    private Customer getLoggedInCustomer(HttpSession session) {
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) throw new RuntimeException("Not logged in");
        return customerService.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        cartService.addToCart(customer, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemsOrdered>> getCartItems(HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        List<ItemsOrdered> items = cartService.getCartItems(customer);
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId, HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        cartService.removeFromCart(customer, itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/increase/{itemId}")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long itemId, HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        cartService.changeQuantity(customer, itemId, 1);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decrease/{itemId}")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long itemId, HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        cartService.changeQuantity(customer, itemId, -1);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy-now")
    public ResponseEntity<?> buyNow(@RequestBody AddToCartRequest request, HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        cartService.buyNow(customer, request.getProductId(), request.getQuantity());
        session.setAttribute("buyNowMode", true);
        return ResponseEntity.ok().header("Location", "/cart").build();
    }
    
    // Test endpoint for debugging cart clearing
    @PostMapping("/test-clear-cart")
    public ResponseEntity<?> testClearCart(HttpSession session) {
        try {
            Customer customer = getLoggedInCustomer(session);
            List<ItemsOrdered> beforeItems = cartService.getCartItems(customer);
            System.out.println("Before clearing: " + beforeItems.size() + " items");
            
            cartService.clearCart(customer);
            
            List<ItemsOrdered> afterItems = cartService.getCartItems(customer);
            System.out.println("After clearing: " + afterItems.size() + " items");
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Cart cleared successfully",
                "beforeCount", beforeItems.size(),
                "afterCount", afterItems.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DTO for add to cart
    public static class AddToCartRequest {
        private Long productId;
        private int quantity;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
} 