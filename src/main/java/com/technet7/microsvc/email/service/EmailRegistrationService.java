package com.technet7.microsvc.email.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.technet7.microsvc.email.dto.EmailRegistrationResponse;
import com.technet7.microsvc.email.exception.EmailAlreadyRegisteredException;
import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;

@Service
public class EmailRegistrationService {
    
    private final EmailRegistrationRepository emailRepository;
    private final PasswordEncoder passwordEncoder;

    public EmailRegistrationService(EmailRegistrationRepository emailRepository, PasswordEncoder passwordEncoder) {
        this.emailRepository = emailRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public EmailRegistrationResponse registerEmail(String email, String username, String password) {
        if (emailRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email " + email + " is already registered");
        }

        String hashed = passwordEncoder.encode(password);
        RegisteredEmail registeredEmail = new RegisteredEmail(email, username, hashed);
        registeredEmail = emailRepository.save(registeredEmail);

        return new EmailRegistrationResponse(
            registeredEmail.getEmail(),
            registeredEmail.getRegistrationDate(),
            "Email registered successfully"
        );
    }
    
    public List<RegisteredEmail> getAllRegisteredEmails() {
        return emailRepository.findAll();
    }
    
    public Optional<RegisteredEmail> getRegisteredEmail(String email) {
        return emailRepository.findByEmail(email);
    }
}