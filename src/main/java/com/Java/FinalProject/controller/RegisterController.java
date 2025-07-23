package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

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
                                  Model model,
                                  HttpSession session) {
        try {
            Customer customer = new Customer();
            customer.setCustomerName(customerName);
            customer.setCustomerEmail(customerEmail);
            customer.setCustomerUsername(customerUsername);
            customer.setCustomerPassword(customerPassword);

            customerService.registerCustomer(customer);
            // Redirect to dashboard with success message, do not auto-login
            return "redirect:/?success=1";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}