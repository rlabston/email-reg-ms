package com.technet7.microsvc.email.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.UserAccount;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.UserAccountRepository;

/**
 * Seeds example users (admin, user, guest) and a RegisteredEmail admin if missing.
 */
@Configuration
public class DataInitializer {

    /**
     * Seed UserAccount entries (used by some legacy paths) and also ensure there is
     * a RegisteredEmail entry for an admin user so that HTTP Basic auth using
     * registered emails works out-of-the-box during development.
     */
    @Bean
    public CommandLineRunner seedUsers(UserAccountRepository repo, EmailRegistrationRepository emailRepo, com.technet7.microsvc.email.repository.RoleRepository roleRepo, PasswordEncoder encoder) {
        return args -> {
            // Ensure roles exist
            if (roleRepo.count() == 0) {
                roleRepo.save(new com.technet7.microsvc.email.model.Role(Roles.ADMIN));
                roleRepo.save(new com.technet7.microsvc.email.model.Role(Roles.USER));
                roleRepo.save(new com.technet7.microsvc.email.model.Role(Roles.GUEST));
            }

            // Ensure a RegisteredEmail exists for admin@example.com so HTTP Basic auth
            // using registered emails works for /admin/** endpoints in dev.
            RegisteredEmail adminReg = emailRepo.findByEmail("admin@example.com").orElseGet(() -> {
                RegisteredEmail r = new RegisteredEmail("admin@example.com", "admin", encoder.encode("admin123"));
                // attach ADMIN and USER roles
                java.util.Set<com.technet7.microsvc.email.model.Role> assigned = new java.util.HashSet<>();
                roleRepo.findByName(Roles.ADMIN).ifPresent(assigned::add);
                roleRepo.findByName(Roles.USER).ifPresent(assigned::add);
                r.setRoles(assigned);
                return emailRepo.save(r);
            });

            // Ensure an admin user profile exists and links to the registered email
            if (repo.findByUsername(adminReg.getEmail()).isEmpty()) {
                UserAccount admin = new UserAccount(adminReg.getEmail(), encoder.encode("admin123"));
                admin.setRegisteredEmail(adminReg);
                repo.save(admin);
            }
        };
    }
}
