package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerUsername(String customerUsername);
    Optional<Customer> findByCustomerEmail(String customerEmail);
    boolean existsByCustomerUsername(String customerUsername);
    boolean existsByCustomerEmail(String customerEmail);
}