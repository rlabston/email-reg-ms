package com.technet7.microsvc.email.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO for chat messages exchanged with the AI chatbot
 */
public class ChatMessage {
    
    private Long id;
    
    @NotBlank(message = "Message content is required")
    private String content;
    
    private String role; // "user" or "assistant"
    
    private LocalDateTime timestamp;
    
    private String conversationId;
    
    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(String content, String role) {
        this();
        this.content = content;
        this.role = role;
    }
    
    public ChatMessage(String content, String role, String conversationId) {
        this(content, role);
        this.conversationId = conversationId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
