package com.technet7.microsvc.email.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technet7.microsvc.email.dto.EmailRegistrationResponse;
import com.technet7.microsvc.email.dto.LoginRequest;
import com.technet7.microsvc.email.dto.LoginResponse;
import com.technet7.microsvc.email.dto.RegisteredEmailDto;
import com.technet7.microsvc.email.dto.RegisteredEmailWithRolesDto;
import com.technet7.microsvc.email.exception.EmailAlreadyRegisteredException;
import com.technet7.microsvc.email.security.JwtService;
import com.technet7.microsvc.email.service.EmailRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/emails")
public class EmailRegistrationController {
    
    private final EmailRegistrationService emailRegistrationService;
    private final JwtService jwtService;
    
    @Autowired
    public EmailRegistrationController(EmailRegistrationService emailRegistrationService, JwtService jwtService) {
        this.emailRegistrationService = emailRegistrationService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerEmail(@Valid @RequestBody com.technet7.microsvc.email.dto.EmailRegistrationRequest request) {
        try {
            return ResponseEntity.ok(emailRegistrationService.registerEmail(request.getEmail(), request.getUsername(), request.getPassword()));
        } catch (EmailAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(new EmailRegistrationResponse(request.getEmail(), null, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<RegisteredEmailDto>> getAllRegisteredEmails() {
        List<RegisteredEmailDto> result = emailRegistrationService.getAllRegisteredEmails()
            .stream()
            .map(e -> new RegisteredEmailDto(
                e.getId(),
                e.getEmail(),
                e.getUsername(),
                e.getRegistrationDate()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Return registered emails with their associated roles as JSON arrays.
     * GET /emails/with-roles
     */
    @GetMapping("/with-roles")
    public ResponseEntity<List<RegisteredEmailWithRolesDto>> getAllWithRoles() {
        List<RegisteredEmailWithRolesDto> result = emailRegistrationService.getAllRegisteredEmails()
            .stream()
            .map(e -> new RegisteredEmailWithRolesDto(
                e.getId(),
                e.getEmail(),
                e.getUsername(),
                e.getRegistrationDate(),
                e.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet())
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return emailRegistrationService.authenticate(request.getEmail(), request.getPassword())
                .map(user -> {
                    // Build authorities from roles
                    var roleNames = user.getRoles().stream().map(r -> r.getName()).toList();
                    var authorities = roleNames.stream()
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                    // Set SecurityContext for this request (useful for /auth/me immediately after)
                    var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Issue JWT for subsequent requests
                    java.util.Set<String> rolesForToken = user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
                    String token = jwtService.generateToken(user.getEmail(), user.getUsername(), rolesForToken);
                    Long expiresIn = jwtService.getExpirationMs();

                    return ResponseEntity.ok(new LoginResponse(user.getEmail(), user.getUsername(), "Login successful", rolesForToken, token, expiresIn));
                })
                .orElse(ResponseEntity.status(401).body(new LoginResponse(request.getEmail(), null, "Invalid credentials", Collections.emptySet())));
    }

    // Optional convenience alias: /emails/list
    @GetMapping("/list")
    public ResponseEntity<List<RegisteredEmailDto>> listAllRegisteredEmails() {
        return getAllRegisteredEmails();
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getRegisteredEmail(@PathVariable String email) {
        return emailRegistrationService.getRegisteredEmail(email)
                .map(e -> ResponseEntity.ok(new RegisteredEmailDto(
                        e.getId(), e.getEmail(), e.getUsername(), e.getRegistrationDate()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete by numeric id. Path is intentionally "id/{id}" to avoid clash with the
     * existing {@code GET /emails/{email}} endpoint which takes an email string.
     */
    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        boolean deleted = emailRegistrationService.deleteById(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Delete by email address via query parameter: /emails/email?email=someone@example.com
     */
    @DeleteMapping("/email")
    public ResponseEntity<?> deleteByEmail(@RequestParam String email) {
        boolean deleted = emailRegistrationService.deleteByEmail(email);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // using EmailRegistrationRequest DTO class
}