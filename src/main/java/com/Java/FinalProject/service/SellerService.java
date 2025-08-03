package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    // Simple encryption using Base64 (for demonstration)
    public String encryptPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // Simple decryption using Base64
    public String decryptPassword(String encryptedPassword) {
        return new String(Base64.getDecoder().decode(encryptedPassword));
    }

    // Helper method to check if password is already encrypted
    public boolean isPasswordEncrypted(String password) {
        if (password == null) return false;
        try {
            // Try to decode as Base64 - if it works, it's likely encrypted
            Base64.getDecoder().decode(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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

        // Encrypt password before saving
        seller.setSellerPassword(encryptPassword(seller.getSellerPassword()));

        // Save the seller
        sellerRepository.save(seller);
    }

    public Seller authenticate(String sellerEmail, String sellerPassword) {
        // Find seller by email
        Optional<Seller> sellerOpt = sellerRepository.findBySellerEmail(sellerEmail);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            
            // Check if password is encrypted
            if (isPasswordEncrypted(seller.getSellerPassword())) {
                // Password is encrypted, decrypt and compare
                String decryptedPassword = decryptPassword(seller.getSellerPassword());
                if (decryptedPassword.equals(sellerPassword)) {
                    return seller;
                }
            } else {
                // Password is plain text (old format), compare directly
                if (seller.getSellerPassword().equals(sellerPassword)) {
                    // Migrate to encrypted password
                    seller.setSellerPassword(encryptPassword(sellerPassword));
                    sellerRepository.save(seller);
                    return seller;
                }
            }
        }
        throw new IllegalArgumentException("Invalid email or password");
    }

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> getSellerById(Long id) {
        return sellerRepository.findById(id);
    }
    
    public Seller saveSeller(Seller seller) {
        // If this is a new seller or password is not encrypted, encrypt it
        if (seller.getSellerId() == null || !isPasswordEncrypted(seller.getSellerPassword())) {
            seller.setSellerPassword(encryptPassword(seller.getSellerPassword()));
        }
        return sellerRepository.save(seller);
    }
    
    // Method to update seller with proper password handling
    public Seller updateSeller(Seller seller) {
        Seller existingSeller = sellerRepository.findById(seller.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        // Update other fields
        existingSeller.setSellerName(seller.getSellerName());
        existingSeller.setSellerEmail(seller.getSellerEmail());
        
        // Handle password update
        if (seller.getSellerPassword() != null && !seller.getSellerPassword().isEmpty()) {
            // If the provided password is different from existing, it's a new password
            if (!seller.getSellerPassword().equals(existingSeller.getSellerPassword())) {
                // Check if the existing password is encrypted
                if (isPasswordEncrypted(existingSeller.getSellerPassword())) {
                    // Compare with decrypted existing password
                    String decryptedExisting = decryptPassword(existingSeller.getSellerPassword());
                    if (!seller.getSellerPassword().equals(decryptedExisting)) {
                        // New password provided, encrypt it
                        existingSeller.setSellerPassword(encryptPassword(seller.getSellerPassword()));
                    }
                } else {
                    // Existing password is plain text, encrypt the new password
                    existingSeller.setSellerPassword(encryptPassword(seller.getSellerPassword()));
                }
            }
            // If password is the same, keep existing (whether encrypted or plain text)
        }
        
        return sellerRepository.save(existingSeller);
    }
    
    public void deleteSeller(Long id) {
        sellerRepository.deleteById(id);
    }
}