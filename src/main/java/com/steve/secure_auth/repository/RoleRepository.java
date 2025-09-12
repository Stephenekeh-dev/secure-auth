package com.steve.secure_auth.repository;

import com.steve.secure_auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // e.g. "ROLE_USER", "ROLE_ADMIN"
}