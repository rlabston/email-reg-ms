package com.technet7.microsvc.email.dto;

import java.util.Set;

public class LoginResponse {
    private String email;
    private String username;
    private String message;
    private Set<String> roles;
    private String token;
    private Long expiresInMs;

    public LoginResponse(String email, String username, String message, Set<String> roles) {
        this.email = email;
        this.username = username;
        this.message = message;
        this.roles = roles;
    }

    public LoginResponse(String email, String username, String message, Set<String> roles, String token, Long expiresInMs) {
        this.email = email;
        this.username = username;
        this.message = message;
        this.roles = roles;
        this.token = token;
        this.expiresInMs = expiresInMs;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(Long expiresInMs) { this.expiresInMs = expiresInMs; }
}
