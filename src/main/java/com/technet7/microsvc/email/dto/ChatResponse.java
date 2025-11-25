package com.technet7.microsvc.email.dto;

import java.util.List;

/**
 * Response DTO containing the chatbot's reply and conversation context
 */
public class ChatResponse {
    
    private String message;
    private String conversationId;
    private Long timestamp;
    private List<ChatMessage> conversationHistory;
    
    // Constructors
    public ChatResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatResponse(String message, String conversationId) {
        this();
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
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<ChatMessage> getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(List<ChatMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
