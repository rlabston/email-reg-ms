package com.technet7.microsvc.email.dto;

public class UserRoleDto {
    private String username;
    private String role;

    public UserRoleDto() {}

    public UserRoleDto(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
