package com.technet7.microsvc.email.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.technet7.microsvc.email.repository.EmailRegistrationRepository;

/**
 * Dev-only helper: if SET_PASSWORD_EMAIL and SET_PASSWORD_RAW env vars are set,
 * update the RegisteredEmail password hash and exit. Safe to leave in code as
 * it only runs when both env vars are present.
 */
@Component
@Profile({"default"})
public class DevPasswordCommand implements CommandLineRunner {
    private final EmailRegistrationRepository emailRepo;
    private final PasswordEncoder encoder;

    public DevPasswordCommand(EmailRegistrationRepository emailRepo, PasswordEncoder encoder) {
        this.emailRepo = emailRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String email = System.getenv("SET_PASSWORD_EMAIL");
        String raw = System.getenv("SET_PASSWORD_RAW");
        if (email == null || email.isBlank() || raw == null || raw.isBlank()) {
            return; // nothing to do
        }

        System.out.println("DevPasswordCommand: updating password for " + email);
        var opt = emailRepo.findByEmail(email);
        if (opt.isEmpty()) {
            System.err.println("DevPasswordCommand: email not found: " + email);
            System.exit(2);
            return;
        }
        var user = opt.get();
        user.setPasswordHash(encoder.encode(raw));
        emailRepo.save(user);
        System.out.println("DevPasswordCommand: password updated successfully");
        // Exit to avoid leaving the server running on accidental invocation
        System.exit(0);
    }
}
