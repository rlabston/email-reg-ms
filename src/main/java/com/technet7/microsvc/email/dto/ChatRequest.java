package com.technet7.microsvc.email.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for sending messages to the chatbot
 */
public class ChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String conversationId;
    
    // Constructors
    public ChatRequest() {}
    
    public ChatRequest(String message) {
        this.message = message;
    }
    
    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
