package com.technet7.microsvc.email.dto;

public class EmailRegistrationResponse {
    private String email;
    private String registrationDate;
    private String message;

    public EmailRegistrationResponse(String email, String registrationDate, String message) {
        this.email = email;
        this.registrationDate = registrationDate;
        this.message = message;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}