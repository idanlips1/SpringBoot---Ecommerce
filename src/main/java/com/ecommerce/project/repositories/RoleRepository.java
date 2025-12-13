package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole roleName);
    Boolean existsByRoleName(AppRole roleName);
}
