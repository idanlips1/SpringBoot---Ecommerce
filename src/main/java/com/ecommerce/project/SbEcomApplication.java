package com.ecommerce.project;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SbEcomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbEcomApplication.class, args);
    }

    // Seed the roles table so /signup can resolve ROLE_USER/ROLE_ADMIN/ROLE_SELLER.
    // Idempotent: inserts each role only when it does not already exist.
    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            for (AppRole appRole : AppRole.values()) {
                if (!roleRepository.existsByRoleName(appRole)) {
                    roleRepository.save(new Role(appRole));
                }
            }
        };
    }

}
