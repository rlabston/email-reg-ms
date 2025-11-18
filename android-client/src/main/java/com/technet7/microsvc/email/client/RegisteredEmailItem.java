package com.technet7.microsvc.email.client;

public class RegisteredEmailItem {
    private Long id;
    private String email;
    private String username;
    private String registrationDate;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getRegistrationDate() { return registrationDate; }
}
