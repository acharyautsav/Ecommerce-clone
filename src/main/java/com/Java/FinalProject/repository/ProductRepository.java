package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all active products by seller ID
     */
    @Query("SELECT p FROM Product p WHERE p.sellerId = :sellerId AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findBySellerIdAndIsActiveTrue(@Param("sellerId") Long sellerId);

    /**
     * Find all products by seller ID (including inactive)
     */
    @Query("SELECT p FROM Product p WHERE p.sellerId = :sellerId ORDER BY p.createdAt DESC")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Find all active products by category
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findByCategoryAndIsActiveTrue(@Param("category") String category);

    /**
     * Find all active products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findAllActiveProducts();

    /**
     * Search products by name (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.isActive = true")
    List<Product> searchByProductName(@Param("searchTerm") String searchTerm);

    /**
     * Count products by seller
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId = :sellerId AND p.isActive = true")
    Long countBySellerIdAndIsActiveTrue(@Param("sellerId") Long sellerId);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true ORDER BY p.price ASC")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}
