package com.technet7.microsvc.email.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing individual chat messages in a conversation
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "role", nullable = false)
    private String role; // "user" or "assistant"
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "token_count")
    private Integer tokenCount;
    
    // Constructors
    public ChatMessageEntity() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessageEntity(String conversationId, String content, String role) {
        this();
        this.conversationId = conversationId;
        this.content = content;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
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
    
    public Integer getTokenCount() {
        return tokenCount;
    }
    
    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }
}
