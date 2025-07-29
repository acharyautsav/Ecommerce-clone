package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findBySellerEmailAndSellerPassword(String sellerEmail, String sellerPassword);
    Optional<Seller> findBySellerEmail(String sellerEmail);
}