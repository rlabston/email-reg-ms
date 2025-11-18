import { Component, signal, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmailRegistrationService } from './services/email-registration.service';
import { AuthService } from './services/auth.service';
import { EmailRegistrationRequest } from './models/email-registration.model';
import { RegisteredEmailDto } from './models/registered-email.model';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('Email Registration');
  protected readonly welcomeMessage = signal('');
  
  email = '';
  username = '';
  password = '';
  emails = signal<RegisteredEmailDto[]>([]);
  selectedEmail = signal<string | null>(null);
  isAdmin = signal(false);
  
  statusMessage = signal('');
  isError = signal(false);
  // isLoading: used for background list loads (not form submission)
  isLoading = signal(false);
  // isSubmitting: used only for registration form submit to avoid locking UI on unrelated actions
  isSubmitting = signal(false);
  listLoading = signal(false);
  showPassword = signal(false);

  private platformId = inject(PLATFORM_ID);

  constructor(
    private emailService: EmailRegistrationService,
    private authService: AuthService,
    private router: Router
  ) {}

  authInitialized = signal(false);
  authed = signal(false);
  currentPath = signal('');

  showRedirectingMessage(): boolean {
    // Only show the redirecting message on the browser when the user is at the root path
    try {
      if (typeof window === 'undefined') return false;
      const p = window.location && window.location.pathname;
      return p === '/' || p === '' || p === '/index.html';
    } catch (e) {
      return false;
    }
  }

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      this.authInitialized.set(true); // SSR: defer
      return;
    }

    // Reset flags
    this.isLoading.set(false);
    this.listLoading.set(false);

    const authenticated = this.authService.isAuthenticated();
    this.authed.set(authenticated);
    if (!authenticated) {
      console.log('[App] Not authenticated; redirecting to /login');
      this.authInitialized.set(true);
      // Defer navigation to avoid interfering with initial navigation/hydration
      setTimeout(() => {
        this.router.navigateByUrl('/login', { replaceUrl: true });
      }, 0);
      // Fallback hard navigation if router navigation is cancelled (rare during hydration)
      setTimeout(() => {
        if (!this.router.url.startsWith('/login')) {
          window.location.assign('/login');
        }
      }, 750);
      return;
    }

    // Try to ensure we have roles in the client; if not, fetch /auth/me
    this.emailService.getCurrentUser().subscribe({
      next: (me) => {
        try {
          if (me && me.authenticated) {
            // me.username is the principal name (email)
            this.authService.setSession(me.username, me.username, me.roles || []);
          }
        } catch (e) {
          // ignore
        }
        // Prefer a human display name when it's distinct from the email; otherwise show the email
        const storedUsername = this.authService.getUsername();
        const storedEmail = this.authService.getEmail();
        const displayName = (storedUsername && storedEmail && storedUsername !== storedEmail)
          ? storedUsername
          : (storedEmail || 'User');
        this.welcomeMessage.set(`Welcome, ${displayName}`);
        this.isAdmin.set(this.authService.isAdmin());
        if (this.isAdmin()) this.loadEmails();
        this.authInitialized.set(true);
      },
      error: (err) => {
        // If /auth/me returns 401 or fails, fall back to existing stored session
        const displayName = this.authService.getUsername() || this.authService.getEmail() || 'User';
        this.welcomeMessage.set(`Welcome, ${displayName}`);
        this.isAdmin.set(this.authService.isAdmin());
        if (this.isAdmin()) this.loadEmails();
        this.authInitialized.set(true);
      }
    });
  }

  selectEmail(e: RegisteredEmailDto): void {
    this.selectedEmail.set(this.selectedEmail() === e.email ? null : e.email);
  }

  deleteSelected(): void {
    const email = this.selectedEmail();
    if (!email) return;
    this.emailService.deleteByEmail(email).subscribe({
      next: () => {
        this.selectedEmail.set(null);
        this.loadEmails();
      },
      error: (err) => {
        console.error('Delete failed', err);
        this.showError('Delete failed: ' + (err.message || 'Unknown'));
      }
    });
  }

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

  this.isSubmitting.set(true);

    const request: EmailRegistrationRequest = {
      email: this.email.trim(),
      username: this.username.trim(),
      password: this.password.trim()
    };

    this.emailService.registerEmail(request).subscribe({
      next: (response) => {
        this.isSubmitting.set(false);
        this.showSuccess(`Registration successful! Welcome, ${response.username}`);
        this.clearForm();
        // Optionally refresh list
        this.loadEmails();
      },
      error: (error) => {
        this.isSubmitting.set(false);
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

  loadEmails(): void {
    this.listLoading.set(true);
    this.emailService.getAllRegisteredEmails().subscribe({
      next: (list) => {
        this.emails.set(list);
        this.listLoading.set(false);
      },
      error: (error) => {
        console.error('Load emails error:', error);
        this.listLoading.set(false);
        if (error.status === 0) {
          this.showError('Cannot connect to server. Please check your connection.');
        } else {
          this.showError(`Failed to load list: ${error.message || 'Unknown error'}`);
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

  logout($event?: Event): void {
    // Prevent any form submission if called from within a form
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    
    console.log('=== LOGOUT CLICKED ===');
    
    if (isPlatformBrowser(this.platformId)) {
      // Immediately clear local storage via auth service
      this.authService.clear();
      
      console.log('LocalStorage cleared immediately');
      
      // Call server-side logout endpoint (but don't wait for it)
      this.emailService.logout().subscribe({
        next: (response) => {
          console.log('Server logout successful:', response);
        },
        error: (error) => {
          console.warn('Server logout failed (non-critical):', error);
        }
      });
      
      // Perform client logout immediately
      this.performClientLogout();
    } else {
      // Server-side rendering context
      this.performClientLogout();
    }
  }

  private performClientLogout(): void {
    try {
      console.log('=== PERFORMING CLIENT LOGOUT ===');
      
      // Clear form and state
      this.emails.set([]);
      this.clearForm();
      this.statusMessage.set('');
      this.welcomeMessage.set('');
      this.isSubmitting.set(false);
      this.isLoading.set(false);
      this.authed.set(false);
      
      console.log('State cleared, forcing hard navigation to /login');
      // Force full page navigation to ensure guard re-evaluates and no cached UI remains
      window.location.assign('/login');
    } catch (error) {
      console.error('Client logout error:', error);
      // Force page reload as fallback
      localStorage.clear(); // Clear all storage as last resort
      window.location.href = '/login';
    }
  }
}
