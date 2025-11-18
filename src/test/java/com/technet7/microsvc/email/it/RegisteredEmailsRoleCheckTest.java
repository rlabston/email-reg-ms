package com.technet7.microsvc.email.it;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.Role;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.RoleRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
    })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class RegisteredEmailsRoleCheckTest {

    @Autowired
    EmailRegistrationRepository emailRepo;

    @Autowired
    RoleRepository roleRepo;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setup() {
        // Start from a clean DB for each test. Delete dependent tables first to
        // avoid foreign-key constraint issues in the in-memory schema.
        jdbc.execute("DELETE FROM user_profiles");
        jdbc.execute("DELETE FROM user_role_link");
        jdbc.execute("DELETE FROM roles");
        emailRepo.deleteAll();

        // create canonical roles
        Role user = roleRepo.save(new Role("USER"));
        Role admin = roleRepo.save(new Role("ADMIN"));

        // create some users with roles
        RegisteredEmail u1 = new RegisteredEmail("user1@example.com", "user1", "pw");
        u1.setRoles(Set.of(user));

        RegisteredEmail u2 = new RegisteredEmail("admin@example.com", "admin", "pw");
        u2.setRoles(Set.of(admin, user));

        RegisteredEmail u3 = new RegisteredEmail("user2@example.com", "user2", "pw");
        u3.setRoles(Set.of(user));

        emailRepo.save(u1);
        emailRepo.save(u2);
        emailRepo.save(u3);
    }

    @Test
    void allRegisteredEmailsHaveAtLeastOneRole() {
        for (RegisteredEmail re : emailRepo.findAll()) {
            assertFalse(re.getRoles() == null || re.getRoles().isEmpty(),
                "RegisteredEmail " + re.getEmail() + " has no roles");
        }
    }
}
