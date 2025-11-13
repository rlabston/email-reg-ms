package com.technet7.microsvc.email.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.technet7.microsvc.email.model.RegisteredEmail;

@Repository
public interface EmailRegistrationRepository extends JpaRepository<RegisteredEmail, Long> {
    Optional<RegisteredEmail> findByEmail(String email);
    boolean existsByEmail(String email);
}