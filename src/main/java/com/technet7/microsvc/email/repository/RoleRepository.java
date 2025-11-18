package com.technet7.microsvc.email.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.technet7.microsvc.email.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
