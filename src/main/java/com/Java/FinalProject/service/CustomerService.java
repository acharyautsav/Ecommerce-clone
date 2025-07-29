package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.repository.CustomerRepository;
import com.Java.FinalProject.repository.CustomerOrderRepository;
import com.Java.FinalProject.entity.CustomerOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

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

    public Customer loginCustomer(String usernameOrEmail, String password) {
        Optional<Customer> customerOpt;
        if (usernameOrEmail.contains("@")) {
            customerOpt = customerRepository.findByCustomerEmail(usernameOrEmail);
        } else {
            customerOpt = customerRepository.findByCustomerUsername(usernameOrEmail);
        }

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String decryptedPassword = decryptPassword(customer.getCustomerPassword());

            if (decryptedPassword.equals(password)) {
                return customer;
            }
        }

        throw new RuntimeException("Invalid username/email or password!");
    }

    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    public Customer saveCustomer(Customer customer) {
        // If this is a new customer or password is not encrypted, encrypt it
        if (customer.getCustomerId() == null || !isPasswordEncrypted(customer.getCustomerPassword())) {
            customer.setCustomerPassword(encryptPassword(customer.getCustomerPassword()));
        }
        return customerRepository.save(customer);
    }
    
    // Helper method to check if password is already encrypted
    private boolean isPasswordEncrypted(String password) {
        if (password == null) return false;
        try {
            // Try to decode as Base64 - if it works, it's likely encrypted
            Base64.getDecoder().decode(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // Method to update customer with proper password handling
    public Customer updateCustomer(Customer customer) {
        Customer existingCustomer = customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Update other fields
        existingCustomer.setCustomerName(customer.getCustomerName());
        existingCustomer.setCustomerEmail(customer.getCustomerEmail());
        existingCustomer.setCustomerUsername(customer.getCustomerUsername());
        existingCustomer.setDeliveryAddress(customer.getDeliveryAddress());
        existingCustomer.setDeliveryLatitude(customer.getDeliveryLatitude());
        existingCustomer.setDeliveryLongitude(customer.getDeliveryLongitude());
        
        // Handle password update
        if (customer.getCustomerPassword() != null && !customer.getCustomerPassword().isEmpty()) {
            // If password is provided and it's not already encrypted, encrypt it
            if (!isPasswordEncrypted(customer.getCustomerPassword())) {
                existingCustomer.setCustomerPassword(encryptPassword(customer.getCustomerPassword()));
            } else {
                existingCustomer.setCustomerPassword(customer.getCustomerPassword());
            }
        }
        
        return customerRepository.save(existingCustomer);
    }
    @Transactional
    public void deleteCustomer(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customerOrderRepository.deleteByCustomer(customer);
            customerRepository.deleteById(id);
        }
    }
}