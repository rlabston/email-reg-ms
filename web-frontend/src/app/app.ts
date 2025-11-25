import { Component, signal, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { EmailRegistrationService } from './services/email-registration.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly welcomeMessage = signal('Email Registration Service');
  
  menuOpen = signal(false);
  isAdmin = signal(false);
  authed = signal(false);

  private platformId = inject(PLATFORM_ID);

  constructor(
    private authService: AuthService,
    private emailService: EmailRegistrationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // Ensure any static login overlay injected by prerender is removed
    try {
      const staticLogin = document.getElementById('static-login');
      if (staticLogin) staticLogin.style.display = 'none';
    } catch (e) { /* ignore */ }

    const authenticated = this.authService.isAuthenticated();
    this.authed.set(authenticated);
    
    if (authenticated) {
      // Try to get current user info to set roles
      this.emailService.getCurrentUser().subscribe({
        next: (me) => {
          if (me && me.authenticated) {
            this.authService.setSession(me.username, me.username, me.roles || []);
          }
          const displayName = this.authService.getUsername() || this.authService.getEmail() || 'User';
          this.welcomeMessage.set(`Welcome, ${displayName}`);
          this.isAdmin.set(this.authService.isAdmin());
        },
        error: () => {
          const displayName = this.authService.getUsername() || this.authService.getEmail() || 'User';
          this.welcomeMessage.set(`Welcome, ${displayName}`);
          this.isAdmin.set(this.authService.isAdmin());
        }
      });
    }
  }

  toggleMenu(): void {
    this.menuOpen.set(!this.menuOpen());
  }

  navigateTo(path: string): void {
    this.menuOpen.set(false);
    this.router.navigate([path]);
  }

  logout($event?: Event): void {
    if ($event) {
      $event.preventDefault();
      $event.stopPropagation();
    }
    
    if (isPlatformBrowser(this.platformId)) {
      this.authService.clear();
      
      this.emailService.logout().subscribe({
        next: () => console.log('Server logout successful'),
        error: (error) => console.warn('Server logout failed (non-critical):', error)
      });
      
      this.authed.set(false);
      this.menuOpen.set(false);
      this.welcomeMessage.set('Email Registration Service');
      
      window.location.assign('/login');
    }
  }
}
