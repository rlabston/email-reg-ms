package com.technet7.microsvc.email.dto;

import java.util.Set;

public class RoleAssignmentResponse {
    private String username;
    private Set<String> roles;

    public RoleAssignmentResponse() {}

    public RoleAssignmentResponse(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
