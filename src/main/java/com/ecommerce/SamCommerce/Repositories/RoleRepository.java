package com.ecommerce.SamCommerce.Repositories;

import com.ecommerce.SamCommerce.Entity.AppRole;
import com.ecommerce.SamCommerce.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(AppRole appRole);
}
