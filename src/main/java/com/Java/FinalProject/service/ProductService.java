package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add a new product
     */
    public Product addProduct(String productName, String category, BigDecimal price, String description, Long sellerId) {
        // Validation
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }

        // Create and save product
        Product product = new Product(
                productName.trim(),
                category.trim(),
                price,
                description != null ? description.trim() : "",
                sellerId
        );

        return productRepository.save(product);
    }

    /**
     * Get all products for a specific seller
     */
    public List<Product> getProductsBySeller(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        return productRepository.findBySellerIdAndIsActiveTrue(sellerId);
    }

    /**
     * Get all active products
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActiveProducts();
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        return productRepository.findByCategoryAndIsActiveTrue(category.trim());
    }

    /**
     * Search products by name
     */
    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveProducts();
        }
        return productRepository.searchByProductName(searchTerm.trim());
    }

    /**
     * Get product by ID
     */
    public Optional<Product> getProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        return productRepository.findById(productId);
    }

    /**
     * Update product
     */
    public Product updateProduct(Long productId, String productName, String category, BigDecimal price, String description) {
        Optional<Product> existingProduct = getProductById(productId);

        if (existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = existingProduct.get();

        // Validation and updates
        if (productName != null && !productName.trim().isEmpty()) {
            product.setProductName(productName.trim());
        }

        if (category != null && !category.trim().isEmpty()) {
            product.setCategory(category.trim());
        }

        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(price);
        }

        if (description != null) {
            product.setDescription(description.trim());
        }

        return productRepository.save(product);
    }

    /**
     * Delete product (soft delete)
     */
    public boolean deleteProduct(Long productId, Long sellerId) {
        Optional<Product> productOpt = getProductById(productId);

        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = productOpt.get();

        // Verify seller owns the product
        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Seller can only delete their own products");
        }

        // Soft delete
        product.setIsActive(false);
        productRepository.save(product);
        return true;
    }

    /**
     * Get product count for seller
     */
    public Long getProductCountBySeller(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        return productRepository.countBySellerIdAndIsActiveTrue(sellerId);
    }

    /**
     * Get products by price range
     */
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null) minPrice = 0.0;
        if (maxPrice == null) maxPrice = Double.MAX_VALUE;

        if (minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
            throw new IllegalArgumentException("Invalid price range");
        }

        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
}
