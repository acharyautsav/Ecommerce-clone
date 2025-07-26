package com.Java.FinalProject.controller;


import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Show all products (public homepage)
     */
    @GetMapping
    public String showAllProducts(Model model) {
        List<Product> products = productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "products"; // products.html template
    }

    /**
     * Get all products (API endpoint)
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    public String getProductsByCategory(@PathVariable String category, Model model) {
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        return "products-by-category"; // products-by-category.html template
    }

    /**
     * Get products by category (API endpoint)
     */
    @GetMapping("/api/category/{category}")
    @ResponseBody
    public ResponseEntity<List<Product>> getProductsByCategoryApi(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String q, Model model) {
        List<Product> products;

        if (q != null && !q.trim().isEmpty()) {
            products = productService.searchProducts(q);
            model.addAttribute("searchTerm", q);
        } else {
            products = productService.getAllActiveProducts();
        }

        model.addAttribute("products", products);
        return "products"; // Use products.html template for search results
    }

    /**
     * Search products (API endpoint)
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchProductsApi(@RequestParam(required = false) String q) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Use the new method to get product and sellerName
            List<Map<String, Object>> products = productService.searchProductsWithSellerName(q);
            response.put("success", true);
            response.put("products", products);
            response.put("count", products.size());
            response.put("searchTerm", q);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get product details
     */
    @GetMapping("/{productId}")
    public String getProductDetails(@PathVariable Long productId, Model model) {
        Optional<Product> product = productService.getProductById(productId);

        if (product.isPresent() && product.get().getIsActive()) {
            model.addAttribute("product", product.get());
            return "product-details"; // product-details.html template
        } else {
            return "product-not-found"; // product-not-found.html template
        }
    }

    /**
     * Get product details (API endpoint)
     */
    @GetMapping("/api/{productId}")
    @ResponseBody
    public ResponseEntity<Product> getProductDetailsApi(@PathVariable Long productId) {
        Optional<Product> product = productService.getProductById(productId);

        if (product.isPresent() && product.get().getIsActive()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get products by price range
     */
    @GetMapping("/api/price-range")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductsByPriceRange(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            response.put("success", true);
            response.put("products", products);
            response.put("count", products.size());
            response.put("minPrice", minPrice);
            response.put("maxPrice", maxPrice);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get product categories
     */
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> response = new HashMap<>();

        // Define available categories
        String[] categories = {
                "Electronics",
                "Fashion",
                "Home & Garden",
                "Sports",
                "Books",
                "Beauty"
        };

        response.put("success", true);
        response.put("categories", categories);

        return ResponseEntity.ok(response);
    }
}
