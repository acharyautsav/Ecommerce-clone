package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.SuperAdmin;
import com.Java.FinalProject.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;

    public Optional<SuperAdmin> authenticate(String username, String password) {
        Optional<SuperAdmin> adminOpt = superAdminRepository.findByUsername(username);
        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            return adminOpt;
        }
        return Optional.empty();
    }

    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    public SuperAdmin saveSuperAdmin(SuperAdmin superAdmin) {
        return superAdminRepository.save(superAdmin);
    }

    public void deleteSuperAdmin(Long id) {
        superAdminRepository.deleteById(id);
    }
} 