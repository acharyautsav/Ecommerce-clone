package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.service.ProductService;
import com.Java.FinalProject.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor

public class SellerRegisterController {

    private final SellerService sellerService;
    private final ProductService productService;

    /* Existing Auth Endpoints */

    @GetMapping("/login")
    public String showSellerLoginForm(Model model) {
        model.addAttribute("seller", new Seller());
        return "seller/login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginSeller(
            @RequestBody Map<String, String> credentials,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!credentials.containsKey("sellerEmail") || !credentials.containsKey("sellerPassword")) {
                throw new IllegalArgumentException("Email and password are required");
            }

            Seller authenticatedSeller = sellerService.authenticate(
                    credentials.get("sellerEmail"),
                    credentials.get("sellerPassword")
            );

            session.setAttribute("seller", authenticatedSeller);
            response.put("status", "success");
            response.put("redirectUrl", "/seller/dashboard");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, String>> registerSeller(
            @RequestBody Seller seller
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            sellerService.registerSeller(seller);
            response.put("status", "success");
            response.put("message", "Registration successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* Seller Dashboard Endpoints */

    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");

        if (seller == null) {
            return "redirect:/";
        }

        List<Product> products = productService.getProductsBySeller(seller.getSellerId());

        model.addAttribute("seller", seller);
        model.addAttribute("products", products);
        model.addAttribute("productCount", productService.getProductCountBySeller(seller.getSellerId()));

        return "seller/sellerDashboard";
    }

    @PostMapping("/addProduct")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        Seller seller = (Seller) session.getAttribute("seller");

        if (seller == null) {
            response.put("status", "error");
            response.put("message", "Session expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Product product = productService.addProduct(
                    productName,
                    category,
                    price,
                    description,
                    seller.getSellerId()
            );

            response.put("status", "success");
            response.put("message", "Product added successfully");
            response.put("redirectUrl", "/seller/dashboard"); // Add this line
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/updateProduct")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProduct(
            @RequestParam Long productId,
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        Seller seller = (Seller) session.getAttribute("seller");

        if (seller == null) {
            response.put("status", "error");
            response.put("message", "Session expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Product product = productService.updateProduct(
                    productId,
                    productName,
                    category,
                    price,
                    description
            );

            response.put("status", "success");
            response.put("message", "Product updated successfully");
            response.put("product", product);
            response.put("redirectUrl", "/seller/dashboard");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/deleteProduct")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @RequestParam Long productId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        Seller seller = (Seller) session.getAttribute("seller");

        if (seller == null) {
            response.put("status", "error");
            response.put("message", "Session expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            boolean deleted = productService.deleteProduct(productId, seller.getSellerId());
            response.put("status", deleted ? "success" : "error");
            response.put("message", deleted ? "Product deleted successfully" : "Failed to delete product");
            response.put("redirectUrl", "/seller/dashboard");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("seller");
        session.invalidate();
        return "redirect:/";
    }

    /* Additional Product Endpoints */

    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<List<Product>> getSellerProducts(HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productService.getProductsBySeller(seller.getSellerId()));
    }

    @GetMapping("/products/count")
    @ResponseBody
    public ResponseEntity<Long> getProductCount(HttpSession session) {
        Seller seller = (Seller) session.getAttribute("seller");
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(productService.getProductCountBySeller(seller.getSellerId()));
    }
}