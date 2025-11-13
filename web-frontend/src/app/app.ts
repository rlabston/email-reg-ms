import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmailRegistrationService } from './services/email-registration.service';
import { EmailRegistrationRequest } from './models/email-registration.model';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Email Registration');
  
  email = '';
  username = '';
  password = '';
  
  statusMessage = signal('');
  isError = signal(false);
  isLoading = signal(false);

  constructor(private emailService: EmailRegistrationService) {}

  onRegister(): void {
    // Clear previous status
    this.statusMessage.set('');
    this.isError.set(false);

    // Validate inputs
    if (!this.email || !this.email.trim()) {
      this.showError('Please enter an email address');
      return;
    }

    if (!this.username || !this.username.trim()) {
      this.showError('Please enter a username');
      return;
    }

    if (!this.password || !this.password.trim()) {
      this.showError('Please enter a password');
      return;
    }

    // Basic email validation
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.email.trim())) {
      this.showError('Please enter a valid email address');
      return;
    }

    this.isLoading.set(true);

    const request: EmailRegistrationRequest = {
      email: this.email.trim(),
      username: this.username.trim(),
      password: this.password.trim()
    };

    this.emailService.registerEmail(request).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.showSuccess(`Registration successful! Welcome, ${response.username}`);
        this.clearForm();
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Registration error:', error);
        
        if (error.status === 400) {
          this.showError('Invalid input. Please check your entries.');
        } else if (error.status === 409) {
          this.showError('This email is already registered.');
        } else if (error.status === 0) {
          this.showError('Cannot connect to server. Please check your connection.');
        } else {
          this.showError(`Registration failed: ${error.message || 'Unknown error'}`);
        }
      }
    });
  }

  private showError(message: string): void {
    this.statusMessage.set(message);
    this.isError.set(true);
  }

  private showSuccess(message: string): void {
    this.statusMessage.set(message);
    this.isError.set(false);
  }

  private clearForm(): void {
    this.email = '';
    this.username = '';
    this.password = '';
  }
}
