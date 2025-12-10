package com.technet7.microsvc.email.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.technet7.microsvc.email.dto.EmailRegistrationResponse;
import com.technet7.microsvc.email.exception.EmailAlreadyRegisteredException;
import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.Role;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.RoleRepository;

@Service
public class EmailRegistrationService {
    
    private final EmailRegistrationRepository emailRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(EmailRegistrationService.class);
    
    public EmailRegistrationService(EmailRegistrationRepository emailRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.emailRepository = emailRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public EmailRegistrationResponse registerEmail(String email, String username, String password) {
        return registerEmail(email, username, password, null);
    }

    public EmailRegistrationResponse registerEmail(String email, String username, String password, Set<String> roleNames) {
        if (emailRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email " + email + " is already registered");
        }

        String hashed = passwordEncoder.encode(password);
        RegisteredEmail registeredEmail = new RegisteredEmail(email, username, hashed);

        // Assign roles if provided
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                    .orElseGet(() -> {
                        Role newRole = new Role(name);
                        return roleRepository.save(newRole);
                    }))
                .collect(Collectors.toSet());
            registeredEmail.setRoles(roles);
        }
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

    public Optional<RegisteredEmail> getRegisteredEmailById(Long id) {
        return emailRepository.findById(id);
    }

    public Optional<RegisteredEmail> authenticate(String email, String password) {
        Optional<RegisteredEmail> userOpt = emailRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("Auth failed: email not found: {}", email);
            return Optional.empty();
        }
        RegisteredEmail user = userOpt.get();
        // Guard against null/blank hashes to avoid IllegalArgumentException from encoder
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            log.warn("Auth failed: empty password hash for email {}", email);
            return Optional.empty();
        }
        boolean matches = false;
        try {
            matches = passwordEncoder.matches(password, storedHash);
        } catch (Exception ex) {
            log.error("Auth error comparing password for {}: {}", email, ex.getMessage());
            return Optional.empty();
        }
        if (!matches) {
            log.warn("Auth failed: password mismatch for {}", email);
            return Optional.empty();
        }

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

    /**
     * Update an existing user's information.
     * @param id the user id
     * @param email new email (optional)
     * @param username new username (optional)
     * @param password new password (optional, will be hashed)
     * @param roleNames new set of role names (optional)
     * @return updated RegisteredEmail or empty if not found
     */
    public Optional<RegisteredEmail> updateUser(Long id, String email, String username, String password, Set<String> roleNames) {
        Optional<RegisteredEmail> userOpt = emailRepository.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        RegisteredEmail user = userOpt.get();

        // Update email if provided
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }

        // Update username if provided
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }

        // Update password if provided
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        // Update roles if provided
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                    .orElseGet(() -> {
                        Role newRole = new Role(name);
                        return roleRepository.save(newRole);
                    }))
                .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        RegisteredEmail updated = emailRepository.save(user);
        return Optional.of(updated);
    }
}