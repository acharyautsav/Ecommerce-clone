package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerId");

        // Check if user is logged in
        if (customerId == null) {
            return "redirect:/login";
        }

        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            model.addAttribute("customer", customer);
            return "dashboard";
        }

        // If customer not found in database, redirect to login
        return "redirect:/login";
    }
}