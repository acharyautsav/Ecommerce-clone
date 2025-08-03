package com.Java.FinalProject;

import com.Java.FinalProject.entity.SuperAdmin;
import com.Java.FinalProject.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;


@SpringBootApplication
@RequiredArgsConstructor
public class FinalProjectApplication {


    private final SuperAdminRepository superAdminRepository;

	public static void main(String[] args) {
		SpringApplication.run(FinalProjectApplication.class, args);
	}
	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

    @PostConstruct
    public void initSuperAdmin() {
        if (superAdminRepository.findByUsername("admin").isEmpty()) {
            SuperAdmin admin = new SuperAdmin();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setEmail("admin@example.com");
            superAdminRepository.save(admin);
        }
    }
}
