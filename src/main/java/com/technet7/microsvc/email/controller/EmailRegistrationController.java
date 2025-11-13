package com.technet7.microsvc.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technet7.microsvc.email.dto.EmailRegistrationResponse;
import com.technet7.microsvc.email.exception.EmailAlreadyRegisteredException;
import com.technet7.microsvc.email.service.EmailRegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/emails")
public class EmailRegistrationController {
    
    private final EmailRegistrationService emailRegistrationService;
    
    @Autowired
    public EmailRegistrationController(EmailRegistrationService emailRegistrationService) {
        this.emailRegistrationService = emailRegistrationService;
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
    public ResponseEntity<?> getAllRegisteredEmails() {
        return ResponseEntity.ok(emailRegistrationService.getAllRegisteredEmails());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getRegisteredEmail(@PathVariable String email) {
        return emailRegistrationService.getRegisteredEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // using EmailRegistrationRequest DTO class
}