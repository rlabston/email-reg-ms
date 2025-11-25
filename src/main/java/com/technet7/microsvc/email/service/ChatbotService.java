package com.technet7.microsvc.email.service;

import com.technet7.microsvc.email.dto.ChatMessage;
import com.technet7.microsvc.email.dto.ChatRequest;
import com.technet7.microsvc.email.dto.ChatResponse;
import com.technet7.microsvc.email.dto.MindsDBQueryRequest;
import com.technet7.microsvc.email.model.ChatConversation;
import com.technet7.microsvc.email.model.ChatMessageEntity;
import com.technet7.microsvc.email.repository.ChatConversationRepository;
import com.technet7.microsvc.email.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling AI chatbot interactions
 * Supports OpenAI API integration with conversation history
 */
@Service
public class ChatbotService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final MindsDBService mindsDBService;
    private final RestTemplate restTemplate;
    
    @Value("${chatbot.api.key:}")
    private String apiKey;
    
    @Value("${chatbot.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${chatbot.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${chatbot.max.history:10}")
    private int maxHistory;
    
    @Value("${chatbot.system.message:You are a helpful AI assistant for an email registration system. Help users with questions about registration, authentication, and general support.}")
    private String systemMessage;
    
    public ChatbotService(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            MindsDBService mindsDBService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.mindsDBService = mindsDBService;
        this.restTemplate = new RestTemplate();
    }
    
    @Transactional
    public ChatResponse processMessage(ChatRequest request, String userEmail) {
        // Get or create conversation
        final String conversationId = (request.getConversationId() == null || request.getConversationId().isEmpty()) 
                ? UUID.randomUUID().toString() 
                : request.getConversationId();
        
        ChatConversation conversation = conversationRepository
                .findByConversationId(conversationId)
                .orElseGet(() -> createNewConversation(conversationId, userEmail));
        
        // Save user message
        ChatMessageEntity userMessage = new ChatMessageEntity(
                conversationId, 
                request.getMessage(), 
                "user"
        );
        messageRepository.save(userMessage);
        
        // Get conversation history
        List<ChatMessageEntity> history = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);
        
        // Generate AI response
        String aiResponse = generateAIResponse(history);
        
        // Save AI response
        ChatMessageEntity assistantMessage = new ChatMessageEntity(
                conversationId,
                aiResponse,
                "assistant"
        );
        messageRepository.save(assistantMessage);
        
        // Update conversation metadata
        conversation.setMessageCount(conversation.getMessageCount() + 2);
        conversationRepository.save(conversation);
        
        // Build response
        ChatResponse response = new ChatResponse(aiResponse, conversationId);
        response.setConversationHistory(convertToDTO(history));
        
        return response;
    }
    
    private ChatConversation createNewConversation(String conversationId, String userEmail) {
        ChatConversation conversation = new ChatConversation(conversationId, userEmail);
        return conversationRepository.save(conversation);
    }
    
    private String generateAIResponse(List<ChatMessageEntity> history) {
        // Try MindsDB first
        try {
            String mindsDbResponse = generateMindsDBResponse(history);
            if (mindsDbResponse != null && !mindsDbResponse.isEmpty()) {
                logger.info("Generated response using MindsDB");
                return mindsDbResponse;
            }
        } catch (Exception e) {
            logger.warn("MindsDB response generation failed, trying OpenAI: {}", e.getMessage());
        }
        
        // Fallback to OpenAI if MindsDB fails or API key is configured
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here")) {
            try {
                String openAIResponse = generateOpenAIResponse(history);
                logger.info("Generated response using OpenAI");
                return openAIResponse;
            } catch (Exception e) {
                logger.warn("OpenAI response generation failed, using mock: {}", e.getMessage());
            }
        }
        
        // Final fallback to mock responses
        logger.info("Using mock response (no AI service available)");
        return generateMockResponse(history);
    }
    
    private String generateMindsDBResponse(List<ChatMessageEntity> history) {
        if (history.isEmpty()) {
            return null; // Let it fall through to other methods
        }
        
        ChatMessageEntity lastMessage = history.get(history.size() - 1);
        String userQuestion = lastMessage.getContent();
        
        // Build conversation context
        StringBuilder contextBuilder = new StringBuilder();
        int startIndex = Math.max(0, history.size() - maxHistory);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessageEntity msg = history.get(i);
            contextBuilder.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        String context = contextBuilder.toString();
        
        // Create MindsDB query to generate a response
        // Using MindsDB's text generation capabilities
        String query = String.format(
            "SELECT response FROM chatbot_model WHERE question='%s' AND context='%s'",
            sanitizeForSQL(userQuestion),
            sanitizeForSQL(context)
        );
        
        try {
            MindsDBQueryRequest request = new MindsDBQueryRequest(query);
            Map<String, Object> response = mindsDBService.executeQuery(request);
            
            // Parse MindsDB response
            String aiResponse = extractResponseFromMindsDB(response);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                return aiResponse;
            }
        } catch (Exception e) {
            logger.error("MindsDB query failed: {}", e.getMessage());
            throw e;
        }
        
        return null;
    }
    
    private String generateOpenAIResponse(List<ChatMessageEntity> history) {
        // Build OpenAI API request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", buildMessages(history));
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );
        
        // Extract response message
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return (String) message.get("content");
            }
        }
        
        return "I apologize, but I couldn't generate a proper response at this time.";
    }
    
    private String sanitizeForSQL(String input) {
        if (input == null) {
            return "";
        }
        // Escape single quotes and limit length
        return input.replace("'", "''").substring(0, Math.min(input.length(), 500));
    }
    
    private String extractResponseFromMindsDB(Map<String, Object> mindsDBResponse) {
        try {
            // MindsDB returns data in a specific format
            if (mindsDBResponse.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) mindsDBResponse.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> firstRow = data.get(0);
                    if (firstRow.containsKey("response")) {
                        return (String) firstRow.get("response");
                    }
                }
            }
            
            // Try alternative response structure
            if (mindsDBResponse.containsKey("column_names") && mindsDBResponse.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<String> columnNames = (List<String>) mindsDBResponse.get("column_names");
                @SuppressWarnings("unchecked")
                List<List<Object>> dataRows = (List<List<Object>>) mindsDBResponse.get("data");
                
                int responseIndex = columnNames.indexOf("response");
                if (responseIndex >= 0 && !dataRows.isEmpty()) {
                    return (String) dataRows.get(0).get(responseIndex);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to extract response from MindsDB result: {}", e.getMessage());
        }
        
        return null;
    }
    
    private List<Map<String, String>> buildMessages(List<ChatMessageEntity> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Add system message
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemMessage);
        messages.add(systemMsg);
        
        // Add conversation history (limited to maxHistory)
        int startIndex = Math.max(0, history.size() - maxHistory);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessageEntity msg = history.get(i);
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }
        
        return messages;
    }
    
    private String generateMockResponse(List<ChatMessageEntity> history) {
        if (history.isEmpty()) {
            return "Hello! I'm your AI assistant. How can I help you today?";
        }
        
        ChatMessageEntity lastMessage = history.get(history.size() - 1);
        String userMsg = lastMessage.getContent().toLowerCase();
        
        // Simple rule-based responses for demo
        if (userMsg.contains("help") || userMsg.contains("?")) {
            return "I can help you with:\n" +
                   "• User registration and email verification\n" +
                   "• Account management\n" +
                   "• Password reset\n" +
                   "• General questions about the service\n\n" +
                   "What would you like to know more about?";
        } else if (userMsg.contains("register") || userMsg.contains("sign up")) {
            return "To register, simply provide your email address. " +
                   "You'll receive a verification link to complete the registration process. " +
                   "Would you like to proceed with registration?";
        } else if (userMsg.contains("password") || userMsg.contains("forgot")) {
            return "If you've forgotten your password, you can reset it by:\n" +
                   "1. Clicking 'Forgot Password' on the login page\n" +
                   "2. Entering your registered email\n" +
                   "3. Following the reset link sent to your email\n\n" +
                   "Need help with anything else?";
        } else if (userMsg.contains("thank")) {
            return "You're welcome! Feel free to ask if you need anything else.";
        } else if (userMsg.contains("hello") || userMsg.contains("hi")) {
            return "Hello! How can I assist you today?";
        } else {
            return "I understand you're asking about: " + lastMessage.getContent() + 
                   "\n\nCould you provide more details so I can better assist you? " +
                   "Or type 'help' to see what I can do.";
        }
    }
    
    private List<ChatMessage> convertToDTO(List<ChatMessageEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    ChatMessage dto = new ChatMessage();
                    dto.setId(entity.getId());
                    dto.setContent(entity.getContent());
                    dto.setRole(entity.getRole());
                    dto.setTimestamp(entity.getTimestamp());
                    dto.setConversationId(entity.getConversationId());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteConversation(String conversationId) {
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.findByConversationId(conversationId)
                .ifPresent(conversationRepository::delete);
    }
    
    public List<ChatMessage> getConversationHistory(String conversationId) {
        List<ChatMessageEntity> messages = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);
        return convertToDTO(messages);
    }
}
