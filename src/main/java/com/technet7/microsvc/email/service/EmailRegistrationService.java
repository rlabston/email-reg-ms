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
        // Prefer repository method that fetches roles via JOIN FETCH to avoid N+1
        return emailRepository.findAllWithRoles();
    }
    
    public Optional<RegisteredEmail> getRegisteredEmail(String email) {
        return emailRepository.findByEmail(email);
    }

    public Optional<RegisteredEmail> authenticate(String email, String password) {
        Optional<RegisteredEmail> userOpt = emailRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();
        RegisteredEmail user = userOpt.get();
        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        if (!matches) return Optional.empty();

        return Optional.of(user);
    }

    /**
     * Delete a registered email by its numeric ID.
     * @param id the id to delete
     * @return true if an entity was found and deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (emailRepository.existsById(id)) {
            emailRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Delete a registered email by its email address.
     * @param email the email address to delete
     * @return true if an entity was found and deleted, false if not found
     */
    public boolean deleteByEmail(String email) {
        Optional<RegisteredEmail> opt = emailRepository.findByEmail(email);
        if (opt.isPresent()) {
            emailRepository.delete(opt.get());
            return true;
        }
        return false;
    }
}