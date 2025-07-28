package com.Java.FinalProject.controller;

import com.Java.FinalProject.config.StripeConfig;
import com.Java.FinalProject.repository.CustomerRepository;
import com.Java.FinalProject.service.OrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class StripePaymentController {

    @Autowired
    private StripeConfig stripeConfig;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/stripe/checkout")
    public String stripeCheckout(@RequestParam("amount") String amount, Model model) {
        model.addAttribute("amount", amount);
        model.addAttribute("publishableKey", stripeConfig.getPublishableKey());
        return "stripeCheckout";
    }

    @GetMapping("/stripe/checkout-session")
    public String createCheckoutSession(@RequestParam("amount") String amount, Model model) {
        try {
            double amountDouble = Double.parseDouble(amount);
            long amountInCents = (long) (amountDouble * 100);
            
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:8000/stripe/success")
                    .setCancelUrl("http://localhost:8000/stripe/fail")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Order Payment")
                                            .build())
                                    .setUnitAmount(amountInCents)
                                    .build())
                            .setQuantity(1L)
                            .build())
                    .build();
            
            Session session = Session.create(params);
            
            // Redirect to Stripe's hosted checkout page
            return "redirect:" + session.getUrl();
            
        } catch (Exception e) {
            System.err.println("Error creating checkout session: " + e.getMessage());
            return "redirect:/stripe/fail";
        }
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            String amountStr = request.get("amount").toString();
            System.out.println("=== Payment Intent Creation ===");
            System.out.println("Received amount: " + amountStr);
            
            // Convert amount to cents (Stripe expects amount in smallest currency unit)
            double amountDouble = Double.parseDouble(amountStr);
            long amountInCents = (long) (amountDouble * 100); // Convert to cents
            System.out.println("Amount in cents: " + amountInCents);
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .build();
            
            System.out.println("Creating payment intent with params: " + params);
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println("Payment intent created: " + paymentIntent.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating payment intent: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/stripe/success")
    public String stripeSuccess(HttpSession session, Model model) {
        // Get customer ID from session
        Long customerId = (Long) session.getAttribute("customerId");
        
        if (customerId != null) {
            // Process the payment success - create orders and clear cart
            orderService.processPaymentSuccess(customerId, "STRIPE");
        }
        
        model.addAttribute("message", "Payment successful! Your order has been placed.");
        model.addAttribute("type", "success");
        return "redirect:/customer/dashboard?payment=success";
    }

    @GetMapping("/stripe/fail")
    public String stripeFail(Model model) {
        model.addAttribute("message", "Payment failed. Please try again.");
        model.addAttribute("type", "error");
        return "redirect:/customer/dashboard?payment=failed";
    }
} 