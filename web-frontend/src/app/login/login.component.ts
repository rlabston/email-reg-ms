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
    <div class="app-container">
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
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
      background-size: cover;
      background-attachment: fixed;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      padding: 20px;
      position: relative;
    }

    .app-container::before {
      content: '';
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: 
        radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.1) 0%, transparent 50%);
      pointer-events: none;
      z-index: 0;
    }

    .login-container {
      max-width: 400px;
      width: 100%;
      padding: 40px;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
      position: relative;
      z-index: 10;
    }

    h2 {
      text-align: center;
      margin-bottom: 30px;
      color: #ffffff;
      font-weight: 700;
      font-size: 28px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #e0e0e0;
      font-size: 14px;
    }

    .form-control {
      width: 100%;
      padding: 12px 16px;
      border: 2px solid rgba(255, 255, 255, 0.2);
      background: rgba(255, 255, 255, 0.1);
      color: #ffffff;
      border-radius: 8px;
      font-size: 16px;
      transition: all 0.3s ease;
      outline: none;
      box-sizing: border-box;
    }

    .form-control::placeholder {
      color: rgba(255, 255, 255, 0.5);
    }

    .form-control:focus {
      border-color: #667eea;
      background: rgba(255, 255, 255, 0.15);
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
    }

    .error-message {
      background-color: #f8d7da;
      color: #721c24;
      padding: 12px 16px;
      border-radius: 8px;
      margin-bottom: 15px;
      font-size: 14px;
      border: 1px solid #f5c6cb;
      animation: slideIn 0.3s ease;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .btn-login {
      width: 100%;
      padding: 14px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      margin-top: 10px;
    }

    .btn-login:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
    }

    .btn-login:active:not(:disabled) {
      transform: translateY(0);
    }

    .btn-login:disabled {
      opacity: 0.6;
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
      color: rgba(255, 255, 255, 0.7);
      transition: color 0.3s;
    }

    .toggle-password:hover {
      color: #ffffff;
    }

    @media (max-width: 480px) {
      .login-container {
        padding: 30px 20px;
      }

      h2 {
        font-size: 24px;
      }
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
        // Store token in memory (all user data extracted from token on-demand)
        if (isPlatformBrowser(this.platformId)) {
          if ((response as any).token) {
            this.authService.setToken((response as any).token);
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
