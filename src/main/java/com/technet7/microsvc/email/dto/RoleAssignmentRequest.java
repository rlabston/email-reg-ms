package com.technet7.microsvc.email.dto;

import java.util.Set;

public class RoleAssignmentRequest {
    private Set<String> roles;

    public RoleAssignmentRequest() {}

    public RoleAssignmentRequest(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
