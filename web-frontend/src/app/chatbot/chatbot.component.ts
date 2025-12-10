import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmailRegistrationService } from '../services/email-registration.service';

interface ChatMessage {
  text: string;
  role: 'user' | 'assistant';
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <div class="chat-header">
        <h1>🤖 AI Assistant</h1>
        <p>Ask me anything about email registration</p>
      </div>
      
      <div class="chat-messages" #chatMessages>
        @for (msg of messages(); track $index) {
          <div class="message" [class.user]="msg.role === 'user'" [class.assistant]="msg.role === 'assistant'">
            <div class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="message-content">{{ msg.text }}</div>
          </div>
        }
        
        @if (isTyping()) {
          <div class="message assistant">
            <div class="message-avatar">🤖</div>
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        }
      </div>
      
      <div class="chat-input-container">
        @if (errorMsg()) {
          <div class="error-message">{{ errorMsg() }}</div>
        }
        <div class="chat-input-wrapper">
          <input 
            type="text" 
            class="chat-input" 
            [(ngModel)]="messageInput"
            (keypress)="onKeyPress($event)"
            [disabled]="isSending()"
            placeholder="Type your message..."
            autocomplete="off"
          />
          <button 
            class="send-button" 
            (click)="sendMessage()"
            [disabled]="isSending() || !messageInput.trim()"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      width: 100%;
      max-width: 800px;
      margin: 20px auto;
      background: white;
      border-radius: 20px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      height: 80vh;
    }
    
    .chat-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 20px;
      text-align: center;
    }
    
    .chat-header h1 {
      font-size: 24px;
      margin-bottom: 5px;
    }
    
    .chat-header p {
      font-size: 14px;
      opacity: 0.9;
    }
    
    .chat-messages {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background: #f5f5f5;
    }
    
    .message {
      margin-bottom: 15px;
      display: flex;
      align-items: flex-start;
    }
    
    .message.user {
      flex-direction: row-reverse;
    }
    
    .message-content {
      max-width: 70%;
      padding: 12px 16px;
      border-radius: 18px;
      word-wrap: break-word;
    }
    
    .message.user .message-content {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
    
    .message.assistant .message-content {
      background: white;
      color: #333;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    
    .message-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 10px;
      font-size: 16px;
    }
    
    .message.user .message-avatar {
      background: #667eea;
    }
    
    .message.assistant .message-avatar {
      background: #764ba2;
    }
    
    .chat-input-container {
      padding: 20px;
      background: white;
      border-top: 1px solid #e0e0e0;
    }
    
    .chat-input-wrapper {
      display: flex;
      gap: 10px;
    }
    
    .chat-input {
      flex: 1;
      padding: 12px 16px;
      border: 2px solid #e0e0e0;
      border-radius: 25px;
      font-size: 14px;
      outline: none;
      transition: border-color 0.3s;
    }
    
    .chat-input:focus {
      border-color: #667eea;
    }
    
    .send-button {
      padding: 12px 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 25px;
      font-size: 14px;
      cursor: pointer;
      transition: transform 0.2s;
    }
    
    .send-button:hover:not(:disabled) {
      transform: scale(1.05);
    }
    
    .send-button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: scale(1);
    }
    
    .typing-indicator {
      padding: 12px 16px;
      background: white;
      border-radius: 18px;
      width: fit-content;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    
    .typing-indicator span {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #667eea;
      margin: 0 2px;
      animation: typing 1.4s infinite;
    }
    
    .typing-indicator span:nth-child(2) {
      animation-delay: 0.2s;
    }
    
    .typing-indicator span:nth-child(3) {
      animation-delay: 0.4s;
    }
    
    @keyframes typing {
      0%, 60%, 100% {
        transform: translateY(0);
      }
      30% {
        transform: translateY(-10px);
      }
    }
    
    .error-message {
      color: #e74c3c;
      font-size: 12px;
      margin-bottom: 10px;
      text-align: center;
    }
  `]
})
export class ChatbotComponent {
  messages = signal<ChatMessage[]>([
    { text: "Hello! I'm your AI assistant. How can I help you today?", role: 'assistant' }
  ]);
  messageInput = '';
  isTyping = signal(false);
  isSending = signal(false);
  errorMsg = signal('');
  conversationId: string | null = null;

  constructor(private emailService: EmailRegistrationService) {}

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  async sendMessage(): Promise<void> {
    const message = this.messageInput.trim();
    if (!message || this.isSending()) return;

    this.errorMsg.set('');
    this.messages.update(msgs => [...msgs, { text: message, role: 'user' }]);
    this.messageInput = '';
    this.isSending.set(true);
    this.isTyping.set(true);

    try {
      const response = await this.emailService.sendChatMessage(message, this.conversationId);
      
      if (response.conversationId) {
        this.conversationId = response.conversationId;
      }

      this.messages.update(msgs => [...msgs, { text: response.message, role: 'assistant' }]);
    } catch (error: any) {
      console.error('Chat error:', error);
      this.errorMsg.set('Failed to send message. Please try again.');
      this.messages.update(msgs => [
        ...msgs,
        { text: "Sorry, I'm having trouble connecting. Please try again later.", role: 'assistant' }
      ]);
    } finally {
      this.isTyping.set(false);
      this.isSending.set(false);
    }
  }
}
