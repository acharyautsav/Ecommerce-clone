package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Simple encryption using Base64 (for demonstration)
    public String encryptPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    // Simple decryption using Base64
    public String decryptPassword(String encryptedPassword) {
        return new String(Base64.getDecoder().decode(encryptedPassword));
    }

    public Customer registerCustomer(Customer customer) {
        // Check if username or email already exists
        if (customerRepository.existsByCustomerUsername(customer.getCustomerUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        if (customerRepository.existsByCustomerEmail(customer.getCustomerEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // Encrypt password before saving
        customer.setCustomerPassword(encryptPassword(customer.getCustomerPassword()));

        return customerRepository.save(customer);
    }

    public Customer loginCustomer(String username, String password) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerUsername(username);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String decryptedPassword = decryptPassword(customer.getCustomerPassword());

            if (decryptedPassword.equals(password)) {
                return customer;
            }
        }

        throw new RuntimeException("Invalid username or password!");
    }

    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }
}