package com.Java.FinalProject.repository;

import com.Java.FinalProject.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    Optional<SuperAdmin> findByUsername(String username);
    Optional<SuperAdmin> findByEmail(String email);
} 