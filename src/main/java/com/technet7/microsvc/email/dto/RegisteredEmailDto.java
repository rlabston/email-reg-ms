package com.technet7.microsvc.email.dto;

public class RegisteredEmailDto {
    private Long id;
    private String email;
    private String username;
    private String registrationDate;

    public RegisteredEmailDto(Long id, String email, String username, String registrationDate) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.registrationDate = registrationDate;
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
}
