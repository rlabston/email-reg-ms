import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-contact',
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="contact-container">
      <div class="contact-box">
        <h1 class="title">Contact Us</h1>
        <p class="subtitle">We'd love to hear from you</p>

        @if (sent()) {
          <div class="success-message">
            <span class="success-text">✓ Message sent successfully!</span>
          </div>
        }

        <div class="form-container">
          <label class="label">Name</label>
          <input
            type="text"
            class="input"
            placeholder="Your name"
            [(ngModel)]="formData.name"
            [disabled]="loading()"
          />

          <label class="label">Email</label>
          <input
            type="email"
            class="input"
            placeholder="Your email"
            [(ngModel)]="formData.email"
            [disabled]="loading()"
          />

          <label class="label">Message</label>
          <textarea
            class="input message-input"
            placeholder="Your message"
            [(ngModel)]="formData.message"
            [disabled]="loading()"
            rows="6"
          ></textarea>

          @if (error()) {
            <p class="error-text">{{ error() }}</p>
          }

          <button
            class="send-button"
            [class.send-button-disabled]="loading()"
            [disabled]="loading()"
            (click)="handleSendMessage()"
          >
            @if (loading()) {
              <span class="spinner"></span>
            } @else {
              Send Message
            }
          </button>
        </div>

        <div class="contact-info-container">
          <h2 class="contact-info-title">Direct Contact</h2>
          
          <div class="contact-info">
            <span class="contact-info-label">Email:</span>
            <span class="contact-info-value">info@technet7.com</span>
          </div>

          <div class="contact-info">
            <span class="contact-info-label">Phone:</span>
            <span class="contact-info-value">+1 (555) 123-4567</span>
          </div>

          <div class="contact-info">
            <span class="contact-info-label">Address:</span>
            <span class="contact-info-value">123 Tech Street, San Francisco, CA 94105</span>
          </div>

          <div class="contact-info">
            <span class="contact-info-label">Hours:</span>
            <span class="contact-info-value">Mon-Fri: 9AM - 6PM PST</span>
          </div>
        </div>

        <div class="back-button-container">
          <a routerLink="/home" class="back-button">← Back to Home</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .contact-container {
      min-height: 100vh;
      padding: 20px;
      background: url('/assets/cityscape.png') center/cover no-repeat;
      background-attachment: fixed;
      background-color: #1a2332;
    }

    .contact-box {
      max-width: 700px;
      margin: 0 auto;
      background: rgba(34, 44, 58, 0.9);
      border-radius: 12px;
      padding: 40px;
      border: 1px solid #4a5568;
    }

    .title {
      color: #ffd700;
      font-size: 32px;
      font-weight: bold;
      margin-bottom: 8px;
      text-align: center;
    }

    .subtitle {
      color: #fff;
      font-size: 16px;
      text-align: center;
      margin-bottom: 24px;
      opacity: 0.8;
    }

    .success-message {
      background: rgba(76, 175, 80, 0.2);
      border: 2px solid #4caf50;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 20px;
      text-align: center;
    }

    .success-text {
      color: #4caf50;
      font-weight: 600;
    }

    .form-container {
      margin-bottom: 32px;
    }

    .label {
      display: block;
      color: #ffd700;
      font-size: 14px;
      font-weight: 600;
      margin-top: 16px;
      margin-bottom: 8px;
    }

    .input {
      width: 100%;
      padding: 12px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid #4a5568;
      border-radius: 6px;
      color: #fff;
      font-size: 14px;
      transition: all 0.3s;
    }

    .input:focus {
      outline: none;
      border-color: #ffd700;
      background: rgba(255, 255, 255, 0.15);
    }

    .input::placeholder {
      color: #999;
    }

    .input:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .message-input {
      min-height: 120px;
      resize: vertical;
    }

    .error-text {
      color: #ff6b6b;
      font-size: 14px;
      margin-top: 8px;
    }

    .send-button {
      width: 100%;
      padding: 14px;
      margin-top: 20px;
      background: #ffd700;
      color: #1a2332;
      border: none;
      border-radius: 6px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .send-button:hover:not(:disabled) {
      background: #ffed4e;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
    }

    .send-button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .spinner {
      display: inline-block;
      width: 20px;
      height: 20px;
      border: 3px solid rgba(26, 35, 50, 0.3);
      border-top-color: #1a2332;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .contact-info-container {
      padding-top: 24px;
      border-top: 1px solid #4a5568;
    }

    .contact-info-title {
      color: #ffd700;
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .contact-info {
      display: flex;
      margin-bottom: 12px;
    }

    .contact-info-label {
      color: #ffd700;
      font-weight: 600;
      min-width: 80px;
      font-size: 14px;
    }

    .contact-info-value {
      color: #fff;
      font-size: 14px;
    }

    .back-button-container {
      margin-top: 32px;
      text-align: center;
      padding-top: 24px;
      border-top: 1px solid #4a5568;
    }

    .back-button {
      display: inline-block;
      padding: 12px 24px;
      background: transparent;
      color: #ffd700;
      text-decoration: none;
      border: 2px solid #ffd700;
      border-radius: 6px;
      font-weight: 600;
      transition: all 0.3s;
    }

    .back-button:hover {
      background: #ffd700;
      color: #1a2332;
      transform: translateY(-2px);
    }

    @media (max-width: 768px) {
      .contact-box {
        padding: 20px;
      }

      .title {
        font-size: 24px;
      }

      .contact-info {
        flex-direction: column;
      }

      .contact-info-label {
        margin-bottom: 4px;
      }
    }
  `]
})
export class ContactComponent {
  formData = { name: '', email: '', message: '' };
  loading = signal(false);
  sent = signal(false);
  error = signal<string | null>(null);

  async handleSendMessage() {
    if (!this.formData.name || !this.formData.email || !this.formData.message) {
      this.error.set('Please fill in all fields');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    try {
      // TODO: Call API to send contact message
      // const response = await ApiService.sendContactMessage(this.formData);
      // if (response.success) {
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API call
      this.sent.set(true);
      this.formData = { name: '', email: '', message: '' };
      setTimeout(() => this.sent.set(false), 3000);
      // }
    } catch (err: any) {
      this.error.set(err.message || 'Failed to send message');
    } finally {
      this.loading.set(false);
    }
  }
}
