package com.technet7.microsvc.email.dto;

import java.util.Set;

public class RegisteredEmailWithRolesDto {
    private Long id;
    private String email;
    private String username;
    private String registrationDate;
    private Set<String> roles;

    public RegisteredEmailWithRolesDto(Long id, String email, String username, String registrationDate, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.registrationDate = registrationDate;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
