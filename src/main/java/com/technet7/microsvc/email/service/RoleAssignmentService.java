package com.technet7.microsvc.email.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.Role;
import com.technet7.microsvc.email.model.UserAccount;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.RoleRepository;
import com.technet7.microsvc.email.repository.UserAccountRepository;

@Service
public class RoleAssignmentService {

    private final EmailRegistrationRepository emailRepo;
    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public RoleAssignmentService(EmailRegistrationRepository emailRepo, UserAccountRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.emailRepo = emailRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Assign roles to a user identified by their registered email address.
     * If no UserAccount exists for the corresponding username, a new one will be created with
     * a random password (encoded) so that the account can be managed later.
     *
    * Behavior:
    * - The username used for the UserAccount is the registered email address from
    *   the `registered_emails` table. Previously a separate display-name could be used,
    *   but we now standardize on the email as the key in the users table.
     *
     * @param email the email address to lookup in the registration table
     * @param roles the roles to assign (replaces existing roles)
     * @return true if assignment succeeded, false if the email was not found in registration table
     */
    public boolean assignRolesByEmail(String email, Set<String> roles) {
        Optional<RegisteredEmail> regOpt = emailRepo.findByEmail(email);
        if (regOpt.isEmpty()) return false;

        RegisteredEmail reg = regOpt.get();
        // Use the registered email as the canonical username/key for UserAccount
        final String finalUsername = reg.getEmail();

        // Normalize role names
        Set<String> roleNames = roles == null ? Set.of() : new HashSet<>(roles);

        // Validate and resolve Role entities
        Set<Role> resolved = roleNames.stream()
            .map(rn -> roleRepo.findByName(rn).orElseThrow(() -> new IllegalArgumentException("Invalid role: " + rn)))
            .collect(Collectors.toSet());

        // Persist roles on RegisteredEmail (the canonical authority source)
        reg.setRoles(resolved);
        emailRepo.save(reg);

        // Ensure a UserAccount profile exists for the registered email; do not duplicate role state here
        java.util.Optional<UserAccount> userOpt = userRepo.findByUsername(finalUsername);
        if (userOpt.isPresent()) {
            UserAccount existing = userOpt.get();
            // Link the existing UserAccount to the RegisteredEmail if not already linked.
            if (existing.getRegisteredEmail() == null || !finalUsername.equals(existing.getRegisteredEmail().getEmail())) {
                existing.setRegisteredEmail(reg);
                userRepo.save(existing);
            }
        } else {
            String randomPwd = java.util.UUID.randomUUID().toString();
            UserAccount u = new UserAccount(finalUsername, passwordEncoder.encode(randomPwd));
            u.setRegisteredEmail(reg);
            userRepo.save(u);
        }

        return true;
    }

        // Helper to return the RegisteredEmail used for mapping
    public RegisteredEmail getRegisteredEmail(String email) {
        return emailRepo.findByEmail(email).orElse(null);
    }

    // Helper to return a UserAccount by username (or null)
    public UserAccount getUserByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }
}
