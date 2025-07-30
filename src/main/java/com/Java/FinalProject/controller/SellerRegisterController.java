package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.entity.ProductCategory;
import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.service.ProductService;
import com.Java.FinalProject.service.SellerService;
import com.Java.FinalProject.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor

public class SellerRegisterController {

    private final SellerService sellerService;
    private final ProductService productService;
    private final OrderService orderService;

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
            System.out.println("=== Seller Login Success ===");
            System.out.println("Seller ID: " + authenticatedSeller.getSellerId());
            System.out.println("Seller Name: " + authenticatedSeller.getSellerName());
            System.out.println("Session ID: " + session.getId());
            
            response.put("status", "success");
            response.put("redirectUrl", "/seller/dashboard");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Seller login error: " + e.getMessage());
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
        List<ProductCategory> categories = productService.getAllCategories();
        
        // Get pending orders for this seller
        List<com.Java.FinalProject.entity.Order> pendingOrders = orderService.getPendingOrdersBySeller(seller.getSellerId());

        model.addAttribute("seller", seller);
        model.addAttribute("products", products);
        model.addAttribute("productCount", productService.getProductCountBySeller(seller.getSellerId()));
        model.addAttribute("categories", categories);
        model.addAttribute("pendingOrders", pendingOrders);

        return "seller/sellerDashboard";
    }

    @PostMapping("/confirmOrder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmOrder(
            @RequestParam Long orderId,
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
            orderService.confirmOrder(orderId);
            response.put("status", "success");
            response.put("message", "Order confirmed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/shipOrder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> shipOrder(
            @RequestParam Long orderId,
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
            orderService.shipOrder(orderId);
            response.put("status", "success");
            response.put("message", "Order shipped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/addProduct")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile productImage,
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
            String imagePath = null;
            
            // Handle image upload if provided
            if (productImage != null && !productImage.isEmpty()) {
                imagePath = saveProductImage(productImage, seller.getSellerId());
            }

            Product product = productService.addProduct(
                    productName,
                    category,
                    price,
                    description,
                    seller.getSellerId(),
                    imagePath
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
            @RequestParam(required = false) MultipartFile productImage,
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
            String imagePath = null;
            
            // Handle image upload if provided
            if (productImage != null && !productImage.isEmpty()) {
                imagePath = saveProductImage(productImage, seller.getSellerId());
            }

            Product product = productService.updateProduct(
                    productId,
                    productName,
                    category,
                    price,
                    description,
                    imagePath
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

    /**
     * Save uploaded product image to filesystem
     */
    private String saveProductImage(MultipartFile file, Long sellerId) throws IOException {
        // Create upload directory if it doesn't exist
        String uploadDir = "src/main/resources/static/uploads/products/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = "product_" + sellerId + "_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = Paths.get(uploadDir + filename);
        Files.write(filePath, file.getBytes());
        
        // Return relative path for database storage
        return "/uploads/products/" + filename;
    }

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