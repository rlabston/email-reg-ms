import { Component, signal, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmailRegistrationService } from '../services/email-registration.service';
import { EmailRegistrationRequest } from '../models/email-registration.model';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-registration',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="registration-container">
      <div class="registration-card">
        <h1 class="title">{{ title() }}</h1>
        
        <form class="registration-form" (ngSubmit)="onRegister()">
          <div class="input-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              [(ngModel)]="email"
              placeholder="Enter your email"
              [disabled]="isSubmitting()"
              required
            />
          </div>

          <div class="input-group">
            <label for="username">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              [(ngModel)]="username"
              placeholder="Enter your username"
              [disabled]="isSubmitting()"
              required
            />
          </div>

          <div class="input-group">
            <label for="password">Password</label>
            <div class="password-input-wrapper">
              <input
                [type]="showPassword() ? 'text' : 'password'"
                id="password"
                name="password"
                [(ngModel)]="password"
                placeholder="Enter your password"
                [disabled]="isSubmitting()"
                required
              />
              <button
                type="button"
                class="toggle-password"
                (click)="showPassword.set(!showPassword())"
                [disabled]="isSubmitting()"
                [attr.aria-label]="showPassword() ? 'Hide password' : 'Show password'"
              >
                {{ showPassword() ? '👁️' : '👁️‍🗨️' }}
              </button>
            </div>
          </div>

          <div class="input-group" *ngIf="isAdminUser()">
            <label>
              <input
                type="checkbox"
                name="isAdmin"
                [(ngModel)]="makeAdmin"
                [disabled]="isSubmitting()"
              />
              Grant Admin Role
            </label>
          </div>

          <button 
            type="submit" 
            class="register-button"
            [disabled]="isSubmitting()"
          >
            <span *ngIf="!isSubmitting()">{{ submitButtonText() }}</span>
            <span *ngIf="isSubmitting()">{{ isEditMode() ? 'Saving...' : 'Registering...' }}</span>
          </button>

          <div 
            *ngIf="statusMessage()" 
            class="status-message"
            [ngClass]="{ 'error': isError(), 'success': !isError() }"
          >
            {{ statusMessage() }}
          </div>
        </form>
      </div>
    </div>
  `,
  styleUrl: '../app.css'
})
export class RegistrationComponent implements OnInit {
  protected readonly title = signal('Email Registration');
  
  email = '';
  username = '';
  password = '';
  makeAdmin = false;
  userId: number | null = null;
  
  statusMessage = signal('');
  isError = signal(false);
  isSubmitting = signal(false);
  showPassword = signal(false);
  isEditMode = signal(false);
  isAdminUser = signal(false);
  submitButtonText = signal('Register');

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  constructor(private emailService: EmailRegistrationService) {}

  ngOnInit(): void {
    // Check if current user is admin
    this.isAdminUser.set(this.authService.isAdmin());
    
    // Check for edit mode via query params
    this.route.queryParams.subscribe(params => {
      if (params['edit'] && params['id']) {
        this.isEditMode.set(true);
        this.userId = +params['id'];
        this.email = params['email'] || '';
        this.username = params['username'] || '';
        this.makeAdmin = params['roles']?.includes('ADMIN') || false;
        this.title.set('Edit User');
        this.submitButtonText.set('Save Changes');
      }
    });
  }

  onRegister(): void {
    this.statusMessage.set('');
    this.isError.set(false);

    if (!this.email || !this.email.trim()) {
      this.showError('Please enter an email address');
      return;
    }

    if (!this.username || !this.username.trim()) {
      this.showError('Please enter a username');
      return;
    }

    if (!this.isEditMode() && (!this.password || !this.password.trim())) {
      this.showError('Please enter a password');
      return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.email.trim())) {
      this.showError('Please enter a valid email address');
      return;
    }

    this.isSubmitting.set(true);

    if (this.isEditMode() && this.userId) {
      // Update existing user
      const updateRequest = {
        id: this.userId,
        email: this.email.trim(),
        username: this.username.trim(),
        password: this.password.trim() || undefined,
        roles: this.makeAdmin ? ['ADMIN', 'USER'] : ['USER']
      };

      this.emailService.updateUser(updateRequest).subscribe({
        next: (response) => {
          this.isSubmitting.set(false);
          this.showSuccess(`User updated successfully!`);
          setTimeout(() => this.router.navigate(['/emails']), 1500);
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Update error:', error);
          this.handleError(error);
        }
      });
    } else {
      // Register new user
      const request: any = {
        email: this.email.trim(),
        username: this.username.trim(),
        password: this.password.trim()
      };

      if (this.isAdminUser()) {
        request.roles = this.makeAdmin ? ['ADMIN', 'USER'] : ['USER'];
      }

      this.emailService.registerEmail(request).subscribe({
        next: (response) => {
          this.isSubmitting.set(false);
          this.showSuccess(`Registration successful! Welcome, ${response.username}`);
          this.clearForm();
        },
        error: (error) => {
          this.isSubmitting.set(false);
          console.error('Registration error:', error);
          this.handleError(error);
        }
      });
    }
  }

  private handleError(error: any): void {
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
