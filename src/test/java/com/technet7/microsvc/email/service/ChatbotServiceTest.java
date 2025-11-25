package com.technet7.microsvc.email.service;

import com.technet7.microsvc.email.dto.ChatRequest;
import com.technet7.microsvc.email.dto.ChatResponse;
import com.technet7.microsvc.email.model.ChatConversation;
import com.technet7.microsvc.email.model.ChatMessageEntity;
import com.technet7.microsvc.email.repository.ChatConversationRepository;
import com.technet7.microsvc.email.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private MindsDBService mindsDBService;

    @InjectMocks
    private ChatbotService chatbotService;

    private ChatRequest chatRequest;
    private ChatConversation conversation;
    private List<ChatMessageEntity> messageHistory;

    @BeforeEach
    void setUp() {
        chatRequest = new ChatRequest();
        chatRequest.setMessage("Hello, how can I register?");
        
        conversation = new ChatConversation();
        conversation.setConversationId("test-conversation-id");
        conversation.setUserEmail("test@example.com");
        conversation.setMessageCount(0);
        
        messageHistory = new ArrayList<>();
    }

    @Test
    void testProcessMessage_NewConversation() {
        // Arrange
        when(conversationRepository.findByConversationId(anyString()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(ChatConversation.class)))
                .thenReturn(conversation);
        when(messageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(anyString()))
                .thenReturn(messageHistory);

        // Act
        ChatResponse response = chatbotService.processMessage(chatRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMessage());
        assertNotNull(response.getConversationId());
        verify(conversationRepository, times(2)).save(any(ChatConversation.class)); // Create + update count
        verify(messageRepository, times(2)).save(any(ChatMessageEntity.class)); // User + Assistant
    }

    @Test
    void testProcessMessage_ExistingConversation() {
        // Arrange
        chatRequest.setConversationId("test-conversation-id");
        
        when(conversationRepository.findByConversationId("test-conversation-id"))
                .thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(ChatConversation.class)))
                .thenReturn(conversation);
        when(messageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(anyString()))
                .thenReturn(messageHistory);

        // Act
        ChatResponse response = chatbotService.processMessage(chatRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("test-conversation-id", response.getConversationId());
        verify(conversationRepository, times(1)).save(any(ChatConversation.class)); // Update count
        verify(messageRepository, times(2)).save(any(ChatMessageEntity.class));
    }

    @Test
    void testProcessMessage_RegisterQuestion() {
        // Arrange
        chatRequest.setMessage("register");
        
        // Add the user message to history to test the response
        ChatMessageEntity userMsg = new ChatMessageEntity("test-id", "register", "user");
        messageHistory.add(userMsg);
        
        when(conversationRepository.findByConversationId(anyString()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(ChatConversation.class)))
                .thenReturn(conversation);
        when(messageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(anyString()))
                .thenReturn(messageHistory);

        // Act
        ChatResponse response = chatbotService.processMessage(chatRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().toLowerCase().contains("register") || 
                   response.getMessage().toLowerCase().contains("email") ||
                   response.getMessage().toLowerCase().contains("verification"));
    }

    @Test
    void testProcessMessage_HelpQuestion() {
        // Arrange
        chatRequest.setMessage("help");
        
        when(conversationRepository.findByConversationId(anyString()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(ChatConversation.class)))
                .thenReturn(conversation);
        when(messageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(anyString()))
                .thenReturn(messageHistory);

        // Act
        ChatResponse response = chatbotService.processMessage(chatRequest, "test@example.com");

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("help") || 
                   response.getMessage().contains("registration"));
    }

    @Test
    void testGetConversationHistory() {
        // Arrange
        ChatMessageEntity message1 = new ChatMessageEntity("test-id", "Hello", "user");
        ChatMessageEntity message2 = new ChatMessageEntity("test-id", "Hi there!", "assistant");
        messageHistory.add(message1);
        messageHistory.add(message2);
        
        when(messageRepository.findByConversationIdOrderByTimestampAsc("test-id"))
                .thenReturn(messageHistory);

        // Act
        var history = chatbotService.getConversationHistory("test-id");

        // Assert
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals("Hello", history.get(0).getContent());
        assertEquals("user", history.get(0).getRole());
    }

    @Test
    void testDeleteConversation() {
        // Arrange
        when(conversationRepository.findByConversationId("test-id"))
                .thenReturn(Optional.of(conversation));

        // Act
        chatbotService.deleteConversation("test-id");

        // Assert
        verify(messageRepository, times(1)).deleteByConversationId("test-id");
        verify(conversationRepository, times(1)).delete(conversation);
    }

    @Test
    void testProcessMessage_UpdatesMessageCount() {
        // Arrange
        chatRequest.setConversationId("test-conversation-id");
        conversation.setMessageCount(5);
        
        when(conversationRepository.findByConversationId("test-conversation-id"))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(ChatMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(anyString()))
                .thenReturn(messageHistory);
        when(conversationRepository.save(any(ChatConversation.class)))
                .thenReturn(conversation);

        // Act
        chatbotService.processMessage(chatRequest, "test@example.com");

        // Assert
        verify(conversationRepository).save(argThat(conv -> 
            conv.getMessageCount() == 7 // 5 + 2 (user + assistant)
        ));
    }
}
