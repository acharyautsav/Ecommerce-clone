package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.service.ProductService;

@RequiredArgsConstructor
@Controller
public class LoginController {


    private final CustomerService customerService;
    private final ProductService productService;

    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        // Show dashboard to all visitors, not just logged-in users
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId != null) {
            model.addAttribute("customerName", session.getAttribute("customerName"));
        }
        // Fetch all products and categories for the landing page
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("categories", new String[]{"Electronics","Fashion","Home & Garden","Sports","Books","Beauty"});
        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("customerId");
        String customerName = (String) session.getAttribute("customerName");
        if (customerId == null || customerName == null) {
            return "redirect:/";
        }
        model.addAttribute("customerName", customerName);
        model.addAttribute("products", productService.getAllActiveProducts());
        return "customerDashboard";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String usernameOrEmail,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        try {
            Customer customer = customerService.loginCustomer(usernameOrEmail, password);
            session.setAttribute("customerId", customer.getCustomerId());
            session.setAttribute("customerName", customer.getCustomerName());
            return "redirect:/customer/dashboard";
        } catch (RuntimeException e) {
            // Redirect to dashboard with error message
            return "redirect:/?error=1";
        }
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}