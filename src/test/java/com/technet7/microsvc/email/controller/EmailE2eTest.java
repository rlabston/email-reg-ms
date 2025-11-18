package com.technet7.microsvc.email.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class EmailE2eTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmailRegistrationRepository emailRepo;

    @Autowired
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    com.technet7.microsvc.email.repository.RoleRepository roleRepo;

    @BeforeEach
    void setup() {
        emailRepo.deleteAll();
        roleRepo.deleteAll();
        roleRepo.save(new com.technet7.microsvc.email.model.Role("ADMIN"));
        roleRepo.save(new com.technet7.microsvc.email.model.Role("USER"));
    }

    @Test
    void adminCanLoginAndDeleteEmail() throws Exception {
        String adminEmail = "e2e-admin@example.com";
        String adminRaw = "adminpass";
    RegisteredEmail admin = new RegisteredEmail(adminEmail, "Admin", passwordEncoder.encode(adminRaw));
    admin.setRoles(java.util.Set.of(
        roleRepo.findByName("ADMIN").orElseThrow(),
        roleRepo.findByName("USER").orElseThrow()
    ));
        emailRepo.save(admin);

        String targetEmail = "victim@example.com";
        RegisteredEmail victim = new RegisteredEmail(targetEmail, "Victim", passwordEncoder.encode("pw"));
        emailRepo.save(victim);

        // Login via API and assert roles included
        String loginBody = objectMapper.writeValueAsString(java.util.Map.of("email", adminEmail, "password", adminRaw));
        var loginResult = mockMvc.perform(post("/emails/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

        String loginResp = loginResult.getResponse().getContentAsString();
        var map = objectMapper.readValue(loginResp, java.util.Map.class);
        assertThat(map.get("roles")).isInstanceOf(java.util.List.class);
        assertThat(((java.util.List<?>)map.get("roles")).contains("ADMIN")).isTrue();

        // Now delete the victim using admin credentials via HTTP Basic
        mockMvc.perform(delete("/emails/email")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(adminEmail, adminRaw))
                .param("email", targetEmail))
            .andExpect(status().isOk());

        // Verify victim removed
        assertThat(emailRepo.findByEmail(targetEmail)).isEmpty();
    }
}
