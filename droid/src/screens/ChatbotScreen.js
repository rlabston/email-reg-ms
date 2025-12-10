import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text, TextInput, TouchableOpacity, ScrollView, FlatList, ActivityIndicator } from 'react-native';

export default function ChatbotScreen() {
  const [messages, setMessages] = useState([
    {
      id: '1',
      text: "Hello! I'm your AI assistant powered by MindsDB. How can I help you today?",
      isUser: false,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }
  ]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSendMessage = async () => {
    if (!inputText.trim()) return;

    // Add user message
    const userMessage = {
      id: Date.now().toString(),
      text: inputText,
      isUser: true,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsLoading(true);

    try {
      // TODO: Call chatbot API endpoint
      // const response = await ApiService.sendChatbotMessage(inputText);
      
      // Simulate API response
      setTimeout(() => {
        const botMessage = {
          id: (Date.now() + 1).toString(),
          text: "I'm processing your request. In a real implementation, this would be a response from MindsDB.",
          isUser: false,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        };
        setMessages(prev => [...prev, botMessage]);
        setIsLoading(false);
      }, 1000);
    } catch (err) {
      setError(err.message || 'Failed to send message');
      setIsLoading(false);
    }
  };

  const MessageBubble = ({ message }) => (
    <View style={[styles.messageBubbleContainer, message.isUser && styles.userBubbleContainer]}>
      <View style={[styles.messageBubble, message.isUser ? styles.userMessage : styles.botMessage]}>
        <Text style={[styles.messageText, message.isUser && styles.userMessageText]}>
          {message.text}
        </Text>
        <Text style={[styles.timestamp, message.isUser && styles.userTimestamp]}>
          {message.timestamp}
        </Text>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>AI Chatbot</Text>
        <Text style={styles.headerSubtitle}>Powered by MindsDB</Text>
      </View>

      <FlatList
        data={messages}
        renderItem={({ item }) => <MessageBubble message={item} />}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.messagesList}
        inverted={false}
      />

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          placeholder="Type your message..."
          placeholderTextColor="#999"
          value={inputText}
          onChangeText={setInputText}
          editable={!isLoading}
          multiline
        />
        <TouchableOpacity
          style={[styles.sendButton, (!inputText.trim() || isLoading) && styles.sendButtonDisabled]}
          onPress={handleSendMessage}
          disabled={!inputText.trim() || isLoading}
        >
          {isLoading ? (
            <ActivityIndicator color="#1a2332" size="small" />
          ) : (
            <Text style={styles.sendButtonText}>📤</Text>
          )}
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a2332',
  },
  header: {
    backgroundColor: 'rgba(26, 35, 50, 0.95)',
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#ffd700',
  },
  headerTitle: {
    color: '#ffd700',
    fontSize: 22,
    fontWeight: 'bold',
  },
  headerSubtitle: {
    color: '#fff',
    fontSize: 12,
    marginTop: 4,
  },
  messagesList: {
    padding: 16,
    flexGrow: 1,
  },
  messageBubbleContainer: {
    marginVertical: 8,
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  userBubbleContainer: {
    justifyContent: 'flex-end',
  },
  messageBubble: {
    maxWidth: '80%',
    borderRadius: 12,
    padding: 12,
  },
  botMessage: {
    backgroundColor: 'rgba(74, 85, 104, 0.7)',
    borderLeftWidth: 2,
    borderLeftColor: '#ffd700',
  },
  userMessage: {
    backgroundColor: '#ffd700',
  },
  messageText: {
    color: '#fff',
    fontSize: 16,
  },
  userMessageText: {
    color: '#1a2332',
  },
  timestamp: {
    color: '#bbb',
    fontSize: 12,
    marginTop: 4,
  },
  userTimestamp: {
    color: '#5a5a5a',
  },
  errorContainer: {
    backgroundColor: '#ff6b6b',
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  errorText: {
    color: '#fff',
    fontSize: 14,
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: 'rgba(34, 44, 58, 0.9)',
    borderTopWidth: 1,
    borderTopColor: '#4a5568',
    gap: 8,
  },
  input: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    color: '#000',
    fontSize: 14,
    maxHeight: 100,
  },
  sendButton: {
    backgroundColor: '#ffd700',
    borderRadius: 8,
    width: 44,
    height: 44,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendButtonDisabled: {
    opacity: 0.5,
  },
  sendButtonText: {
    fontSize: 20,
  },
});
