package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.ItemsOrdered;
import com.Java.FinalProject.entity.CustomerOrder;
import com.Java.FinalProject.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsOrderedRepository extends JpaRepository<ItemsOrdered, Long> {
    List<ItemsOrdered> findByCustomerOrder(CustomerOrder customerOrder);
    void deleteByProduct(Product product);
} 