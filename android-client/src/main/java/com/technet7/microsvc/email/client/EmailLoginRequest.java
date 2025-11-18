package com.technet7.microsvc.email.client;

public class EmailLoginRequest {
    private String email;
    private String password;

    public EmailLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
