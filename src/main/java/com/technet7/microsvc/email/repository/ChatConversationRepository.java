package com.technet7.microsvc.email.repository;

import com.technet7.microsvc.email.model.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    
    Optional<ChatConversation> findByConversationId(String conversationId);
    
    Optional<ChatConversation> findByUserEmail(String userEmail);
}
