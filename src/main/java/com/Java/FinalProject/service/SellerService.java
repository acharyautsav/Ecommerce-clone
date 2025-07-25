package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.repository.SellerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public void registerSeller(Seller seller) {
        // Validate all fields
        if (seller.getSellerName() == null || seller.getSellerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Business name is required");
        }
        if (seller.getSellerEmail() == null || seller.getSellerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (seller.getSellerPassword() == null || seller.getSellerPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

//        // Check for existing email
//        if (sellerRepository.findBySellerEmail(seller.getSellerEmail()).isPresent()) {
//            throw new IllegalArgumentException("Email already registered");
//        }

        // Save the seller
        sellerRepository.save(seller);
    }

    public Seller authenticate(String sellerEmail, String sellerPassword) {
        // Find seller by email and password
        return sellerRepository.findBySellerEmailAndSellerPassword(sellerEmail, sellerPassword)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
    }

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> getSellerById(Long id) {
        return sellerRepository.findById(id);
    }
    public Seller saveSeller(Seller seller) {
        return sellerRepository.save(seller);
    }
    public void deleteSeller(Long id) {
        sellerRepository.deleteById(id);
    }
}