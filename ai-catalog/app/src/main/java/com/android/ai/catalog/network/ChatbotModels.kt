package com.android.ai.catalog.network

import com.google.gson.annotations.SerializedName

/**
 * Data models for chatbot API integration
 */

data class ChatMessage(
    val id: Long?,
    @SerializedName("conversation_id")
    val conversationId: String,
    val message: String,
    val response: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    val sender: String // "user" or "ai"
)

data class ChatRequest(
    @SerializedName("conversation_id")
    val conversationId: String,
    val message: String
)

data class ChatResponse(
    val response: String,
    @SerializedName("conversation_id")
    val conversationId: String,
    val timestamp: String?
)

data class ConversationHistoryResponse(
    @SerializedName("conversation_id")
    val conversationId: String,
    val messages: List<ChatMessage>
)
