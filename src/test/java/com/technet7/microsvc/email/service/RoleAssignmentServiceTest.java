package com.technet7.microsvc.email.service;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.UserAccount;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.UserAccountRepository;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class RoleAssignmentServiceTest {

    @Autowired
    RoleAssignmentService roleAssignmentService;

    @Autowired
    EmailRegistrationRepository emailRepo;

    @Autowired
    UserAccountRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        userRepo.deleteAll();
        emailRepo.deleteAll();
    }

    @Test
    void assignRoles_createsUserWhenMissing() {
        String email = "newuser@example.com";
        String username = "newuser";
        RegisteredEmail reg = new RegisteredEmail(email, username, "pw");
        emailRepo.save(reg);

        boolean result = roleAssignmentService.assignRolesByEmail(email, Set.of("USER"));
        assertTrue(result);

        // Service uses email as the UserAccount username key
        Optional<UserAccount> acctOpt = userRepo.findByUsername(email);
        assertTrue(acctOpt.isPresent(), "UserAccount should be created");
        UserAccount acct = acctOpt.get();
        assertEquals(Set.of("USER"), acct.getRoles());
    }

    @Test
    void assignRoles_replacesExistingRoles() {
        String email = "exist@example.com";
        String username = "existuser";
        RegisteredEmail reg = new RegisteredEmail(email, username, "pw");
        emailRepo.save(reg);

        // Create UserAccount using email as username (matching service behavior)
        UserAccount existing = new UserAccount(email, passwordEncoder.encode("x"), Set.of("GUEST"));
        userRepo.save(existing);

        boolean result = roleAssignmentService.assignRolesByEmail(email, Set.of("ADMIN", "USER"));
        assertTrue(result);

        UserAccount acct = userRepo.findByUsername(email).orElseThrow();
        assertEquals(Set.of("ADMIN", "USER"), acct.getRoles());
    }

    @Test
    void assignRoles_missingEmailReturnsFalse() {
        boolean result = roleAssignmentService.assignRolesByEmail("doesnotexist@example.com", Set.of("USER"));
        assertFalse(result);
    }

    @Test
    void assignRoles_emptyRoleSetClearsRoles() {
        String email = "emptyroles@example.com";
        String username = "eruser";
        RegisteredEmail reg = new RegisteredEmail(email, username, "pw");
        emailRepo.save(reg);

        // Create UserAccount using email as username (matching service behavior)
        UserAccount existing = new UserAccount(email, passwordEncoder.encode("x"), Set.of("GUEST"));
        userRepo.save(existing);

        boolean result = roleAssignmentService.assignRolesByEmail(email, Set.of());
        assertTrue(result);

        UserAccount acct = userRepo.findByUsername(email).orElseThrow();
        assertTrue(acct.getRoles().isEmpty(), "Roles should be cleared when empty set provided");
    }
}
