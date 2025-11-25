package com.technet7.microsvc.email.controller;

import com.technet7.microsvc.email.dto.ChatMessage;
import com.technet7.microsvc.email.dto.ChatRequest;
import com.technet7.microsvc.email.dto.ChatResponse;
import com.technet7.microsvc.email.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for AI chatbot endpoints
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatbotController {
    
    private final ChatbotService chatbotService;
    
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }
    
    /**
     * Send a message to the chatbot
     * 
     * @param request The chat request containing the user's message
     * @param authentication The authenticated user
     * @return ChatResponse containing the AI's reply
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        String userEmail = authentication != null ? authentication.getName() : "anonymous";
        ChatResponse response = chatbotService.processMessage(request, userEmail);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get conversation history
     * 
     * @param conversationId The conversation ID
     * @return List of chat messages
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @PathVariable String conversationId) {
        
        List<ChatMessage> history = chatbotService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Delete a conversation
     * 
     * @param conversationId The conversation ID to delete
     * @return Success response
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String conversationId) {
        
        chatbotService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Health check endpoint for chatbot service
     * 
     * @return Status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
