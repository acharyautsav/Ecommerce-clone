package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class RegisterController {


    private final CustomerService customerService;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String customerName,
                                  @RequestParam String customerEmail,
                                  @RequestParam String customerUsername,
                                  @RequestParam String customerPassword,
                                  Model model) {
        try {
            Customer customer = new Customer();
            customer.setCustomerName(customerName);
            customer.setCustomerEmail(customerEmail);
            customer.setCustomerUsername(customerUsername);
            customer.setCustomerPassword(customerPassword);

            customerService.registerCustomer(customer);
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}