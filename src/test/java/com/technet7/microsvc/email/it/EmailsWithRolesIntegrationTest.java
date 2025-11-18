package com.technet7.microsvc.email.it;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
    })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class EmailsWithRolesIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmailRegistrationRepository emailRepo;

    @Autowired
    com.technet7.microsvc.email.repository.UserAccountRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    com.technet7.microsvc.email.repository.RoleRepository roleRepo;

    @BeforeEach
    void setup() {
        // Clear user accounts first because user_profiles has a FK to registered_emails
        userRepo.deleteAll();
        emailRepo.deleteAll();
        roleRepo.deleteAll();
        roleRepo.save(new com.technet7.microsvc.email.model.Role("ADMIN"));
        roleRepo.save(new com.technet7.microsvc.email.model.Role("USER"));

    RegisteredEmail admin = new RegisteredEmail("admin@example.com", "admin", passwordEncoder.encode("admin123"));
    admin.setRoles(java.util.Set.of(
        roleRepo.findByName("ADMIN").orElseThrow(),
        roleRepo.findByName("USER").orElseThrow()
    ));

    RegisteredEmail user = new RegisteredEmail("user@example.com", "user", passwordEncoder.encode("user123"));
    user.setRoles(java.util.Set.of(roleRepo.findByName("USER").orElseThrow()));

        emailRepo.save(admin);
        emailRepo.save(user);
    }

    @Test
    void getEmailsWithRoles_returnsJsonArrayWithRoles() throws Exception {
        String url = "http://localhost:" + port + "/emails/with-roles";

        ResponseEntity<List> resp = restTemplate.withBasicAuth("admin@example.com", "admin123")
            .getForEntity(url, List.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        HttpHeaders headers = resp.getHeaders();
        MediaType ct = headers.getContentType();
        assertTrue(ct != null && (ct.includes(MediaType.APPLICATION_JSON) || ct.getSubtype().contains("json")));

        List<?> body = resp.getBody();
        assertTrue(body != null && body.size() >= 2);

        // Inspect first element structure
        Object first = body.get(0);
        assertTrue(first instanceof Map);
        Map<?,?> m = (Map<?,?>) first;
        assertTrue(m.containsKey("email"));
        assertTrue(m.containsKey("roles"));
        Object roles = m.get("roles");
        assertTrue(roles instanceof List || roles instanceof Set);
    }
}
