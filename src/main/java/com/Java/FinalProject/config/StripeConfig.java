package com.Java.FinalProject.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {
    
    @Value("${stripe.secret.key}")
    private String secretKey;
    
    @Value("${stripe.publishable.key}")
    private String publishableKey;
    
    @PostConstruct
    public void initStripe() {
        System.out.println("=== Stripe Configuration ===");
        System.out.println("Secret Key: " + (secretKey != null ? secretKey.substring(0, Math.min(20, secretKey.length())) + "..." : "NULL"));
        System.out.println("Publishable Key: " + (publishableKey != null ? publishableKey.substring(0, Math.min(20, publishableKey.length())) + "..." : "NULL"));
        System.out.println("===========================");
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new RuntimeException("Stripe secret key is not configured!");
        }
        
        if (publishableKey == null || publishableKey.trim().isEmpty()) {
            throw new RuntimeException("Stripe publishable key is not configured!");
        }
        
        Stripe.apiKey = secretKey;
        System.out.println("Stripe initialized successfully!");
    }
    
    public String getPublishableKey() {
        return publishableKey;
    }
} 