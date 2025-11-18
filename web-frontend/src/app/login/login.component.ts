import { Component, signal, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmailRegistrationService } from '../services/email-registration.service';
import { AuthService } from '../services/auth.service';
import { LoginRequest } from '../models/login.model';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h2>Email Registration Login</h2>
      
      <form (ngSubmit)="onLogin()" #loginForm="ngForm">
        <div class="form-group">
          <label for="email">Email:</label>
          <input
            type="email"
            id="email"
            name="email"
            [(ngModel)]="email"
            required
            placeholder="Enter your email"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label for="password">Password:</label>
          <div class="password-input-wrapper">
            <input
              [type]="showPassword() ? 'text' : 'password'"
              id="password"
              name="password"
              [(ngModel)]="password"
              required
              placeholder="Enter your password"
              class="form-control"
            />
            <button
              type="button"
              class="toggle-password"
              (click)="showPassword.set(!showPassword())"
              [attr.aria-label]="showPassword() ? 'Hide password' : 'Show password'"
            >
              {{ showPassword() ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>

        @if (errorMessage()) {
          <div class="error-message">{{ errorMessage() }}</div>
        }

        <button 
          type="submit" 
          [disabled]="isLoading() || !loginForm.form.valid"
          class="btn-login"
        >
          {{ isLoading() ? 'Logging in...' : 'Login' }}
        </button>
      </form>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 400px;
      margin: 100px auto;
      padding: 30px;
      border: 1px solid #ddd;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    h2 {
      text-align: center;
      margin-bottom: 30px;
      color: #333;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 5px;
      font-weight: 600;
      color: #555;
    }

    .form-control {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
      box-sizing: border-box;
    }

    .form-control:focus {
      outline: none;
      border-color: #4CAF50;
    }

    .error-message {
      background-color: #ffebee;
      color: #c62828;
      padding: 10px;
      border-radius: 4px;
      margin-bottom: 15px;
      font-size: 14px;
    }

    .btn-login {
      width: 100%;
      padding: 12px;
      background-color: #4CAF50;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: background-color 0.3s;
    }

    .btn-login:hover:not(:disabled) {
      background-color: #45a049;
    }

    .btn-login:disabled {
      background-color: #cccccc;
      cursor: not-allowed;
    }

    .password-input-wrapper {
      position: relative;
    }

    .password-input-wrapper .form-control {
      padding-right: 45px;
    }

    .toggle-password {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      cursor: pointer;
      font-size: 18px;
      padding: 5px;
      color: #666;
      transition: color 0.3s;
    }

    .toggle-password:hover {
      color: #333;
    }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage = signal('');
  isLoading = signal(false);
  showPassword = signal(false);
  
  private platformId = inject(PLATFORM_ID);

  constructor(
    private emailService: EmailRegistrationService,
    private router: Router,
    private authService: AuthService
  ) {}

  onLogin(): void {
    console.log('[SPA-login] onLogin start', { email: this.email });
    this.errorMessage.set('');

    if (!this.email.trim() || !this.password.trim()) {
      this.errorMessage.set('Please enter both email and password');
      return;
    }

    if (this.password.length < 8) {
      this.errorMessage.set('Password must be at least 8 characters');
      return;
    }

    this.isLoading.set(true);

    const request: LoginRequest = {
      email: this.email.trim(),
      password: this.password.trim()
    };
    console.log('[SPA-login] will send login request', request);

    this.emailService.login(request).subscribe({
      next: (response) => {
        console.log('[SPA-login] login response', response);
        this.isLoading.set(false);
        // Store session (email, username, roles)
        if (isPlatformBrowser(this.platformId)) {
          try {
            this.authService.setSession(response.email, response.username, response.roles || []);
            // Save token if present
            if ((response as any).token) {
              localStorage.setItem('auth_token', (response as any).token);
            }
            if (typeof (response as any).expiresInMs === 'number') {
              const expMs = Number((response as any).expiresInMs);
              localStorage.setItem('auth_token_expires_in_ms', String(expMs));
              localStorage.setItem('auth_token_expires_at', String(Date.now() + expMs));
            }
          } catch (e) {
            console.warn('[SPA-login] session set failed', e);
            try {
              localStorage.setItem('auth_email', response.email);
              localStorage.setItem('auth_username', response.username);
              localStorage.setItem('auth_roles', JSON.stringify(response.roles || []));
              if ((response as any).token) localStorage.setItem('auth_token', (response as any).token);
            } catch (e2) {
              // ignore
            }
          }
        }
        // Navigate to main app
        console.log('[SPA-login] navigating to /');
        this.router.navigate(['/']).catch(err => console.warn('[SPA-login] navigation error', err));
      },
      error: (error) => {
        console.error('[SPA-login] login error', error);
        this.isLoading.set(false);
        if (error.status === 401) {
          this.errorMessage.set('Invalid email or password');
        } else if (error.status === 0) {
          this.errorMessage.set('Cannot connect to server');
        } else {
          this.errorMessage.set('Login failed. Please try again.');
        }
      }
    });
  }
}
