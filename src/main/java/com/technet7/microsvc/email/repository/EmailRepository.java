package com.technet7.microsvc.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.technet7.microsvc.email.model.RegisteredEmail;

public interface EmailRepository extends JpaRepository<RegisteredEmail, Long> {
    boolean existsByEmail(String email);
}