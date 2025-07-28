package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery")
public class DeliveryAddressController {
    
    private final CustomerService customerService;
    
    public DeliveryAddressController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    @GetMapping("/address")
    public String deliveryAddressPage(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) return "redirect:/";
        
        Customer customer = customerService.findById(customerId).orElse(null);
        if (customer == null) return "redirect:/";
        
        model.addAttribute("customerName", customer.getCustomerName());
        return "delivery-address";
    }
    
    @PostMapping("/save-address")
    @ResponseBody
    public String saveDeliveryAddress(@RequestParam String address, 
                                    @RequestParam String latitude, 
                                    @RequestParam String longitude,
                                    HttpSession session) {
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) return "error";
        
        Customer customer = customerService.findById(customerId).orElse(null);
        if (customer == null) return "error";
        
        // Save the delivery address to the customer
        customer.setDeliveryAddress(address);
        customer.setDeliveryLatitude(latitude);
        customer.setDeliveryLongitude(longitude);
        customerService.saveCustomer(customer);
        
        return "success";
    }
} 