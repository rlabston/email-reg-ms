package com.technet7.microsvc.email.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;

public class UpdateUserRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String username;
    
    private String password; // Optional - only if changing password
    
    private Set<String> roles;

    public UpdateUserRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
