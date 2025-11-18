package com.technet7.microsvc.email.controller;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.model.UserAccount;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.repository.UserAccountRepository;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmailRegistrationRepository emailRepo;

    @Autowired
    com.technet7.microsvc.email.repository.RoleRepository roleRepo;

    @Autowired
    UserAccountRepository userRepo;

    @Autowired
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();
        emailRepo.deleteAll();
        roleRepo.deleteAll();
        // ensure canonical roles exist for tests
        roleRepo.save(new com.technet7.microsvc.email.model.Role("ADMIN"));
        roleRepo.save(new com.technet7.microsvc.email.model.Role("USER"));
        roleRepo.save(new com.technet7.microsvc.email.model.Role("GUEST"));
        // Create admin and regular users as RegisteredEmail entries (auth now uses registered_emails)
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
    void assignRoles_successAsAdmin() throws Exception {
        String email = "assignme@example.com";
        String username = "assignme";
    RegisteredEmail reg = new RegisteredEmail(email, username, "pw");
    emailRepo.save(reg);

        String body = objectMapper.writeValueAsString(java.util.Map.of("roles", java.util.List.of("USER","ADMIN")));

        var mvcResult = mockMvc.perform(post("/admin/assign-roles")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@example.com", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", email)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

    String resp = mvcResult.getResponse().getContentAsString();
    var map = objectMapper.readValue(resp, java.util.Map.class);
    // controller returns the canonical usernameKey which in our implementation is the registered email
    assertEquals(email, map.get("username"));
    assertEquals(java.util.Set.of("USER","ADMIN"), new java.util.HashSet<>((java.util.List<?>)map.get("roles")));

    UserAccount acct = userRepo.findByUsername(email).orElseThrow();
    assertEquals(Set.of("USER","ADMIN"), acct.getRoles());
    }

    @Test
    void assignRoles_forbiddenForNonAdmin() throws Exception {
        String email = "other@example.com";
        RegisteredEmail reg = new RegisteredEmail(email, "other", "pw");
        emailRepo.save(reg);

    // user created in setup
        String body = objectMapper.writeValueAsString(java.util.Map.of("roles", java.util.List.of("USER")));

    mockMvc.perform(post("/admin/assign-roles")
    .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@example.com", "user123"))
        .contentType(MediaType.APPLICATION_JSON)
        .param("email", email)
        .content(body))
        .andExpect(status().isForbidden());
    }

    @Test
    void assignRoles_notFoundWhenEmailMissing() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("roles", java.util.List.of("USER")));

    mockMvc.perform(post("/admin/assign-roles")
    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@example.com", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", "no-such@example.com")
                .content(body))
            .andExpect(status().isNotFound());
    }

    @Test
    void assignRoles_invalidRoleRejected() throws Exception {
        String email = "x@example.com";
        RegisteredEmail reg = new RegisteredEmail(email, "x", "pw");
        emailRepo.save(reg);

        String body = objectMapper.writeValueAsString(java.util.Map.of("roles", java.util.List.of("HACKER")));

        mockMvc.perform(post("/admin/assign-roles")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@example.com", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", email)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEndpoints_adminCanDeleteByIdAndEmail() throws Exception {
        // create a registered email
        RegisteredEmail reg = new RegisteredEmail("del@example.com", "deluser", "pw");
        reg = emailRepo.save(reg);

        // delete by id
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/emails/id/" + reg.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@example.com", "admin123")))
            .andExpect(status().isOk());

        // recreate and delete by email
        RegisteredEmail reg2 = new RegisteredEmail("del2@example.com", "deluser2", "pw");
        emailRepo.save(reg2);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/emails/email")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@example.com", "admin123"))
                .param("email", "del2@example.com"))
            .andExpect(status().isOk());
    }

    @Test
    void deleteEndpoints_forbiddenForNonAdmin() throws Exception {
        RegisteredEmail reg = new RegisteredEmail("ndel@example.com", "ndeluser", "pw");
        reg = emailRepo.save(reg);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/emails/id/" + reg.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@example.com", "user123")))
            .andExpect(status().isForbidden());
    }

    @Test
    void loginResponse_includesRoles() throws Exception {
        String email = "adminlogin@example.com";
        String username = "alogin";
        String rawPassword = "secret123";
    RegisteredEmail reg = new RegisteredEmail(email, username, passwordEncoder.encode(rawPassword));
    reg.setRoles(java.util.Set.of(
        roleRepo.findByName("ADMIN").orElseThrow(),
        roleRepo.findByName("USER").orElseThrow()
    ));
        emailRepo.save(reg);

        String body = objectMapper.writeValueAsString(java.util.Map.of("email", email, "password", rawPassword));

        var mvcResult = mockMvc.perform(post("/emails/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();

        String resp = mvcResult.getResponse().getContentAsString();
        var map = objectMapper.readValue(resp, java.util.Map.class);
        // roles should be present and contain ADMIN
        java.util.List<?> roles = (java.util.List<?>) map.get("roles");
        org.junit.jupiter.api.Assertions.assertTrue(roles.contains("ADMIN"));
    }
}
