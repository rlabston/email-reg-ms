import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chatbot-wrapper">
      <h2>AI Chatbot</h2>
      <iframe
        title="Chatbot"
        src="/chatbot.html"
        class="chatbot-frame"
        loading="lazy"
        referrerpolicy="no-referrer"
      ></iframe>
    </div>
  `,
  styles: [`
    .chatbot-wrapper { padding: 16px; max-width: 1200px; margin: 0 auto; }
    h2 { color: #fff; margin-bottom: 12px; font-weight: 600; }
    .chatbot-frame { width: 100%; height: 70vh; border: 2px solid rgba(255,255,255,0.2); border-radius: 12px; background:#000; }
    @media (max-width: 640px){ .chatbot-frame { height: 65vh; } }
  `]
})
export class ChatbotComponent {}
