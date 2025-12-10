import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Memory-only token storage (single source of truth from backend)
  // All user data (email, username, roles) extracted from token on-demand
  private currentToken: string | null = null;
  
  // Observable to notify components when auth state changes
  private authStateChanged = new BehaviorSubject<boolean>(false);
  public authState$: Observable<boolean> = this.authStateChanged.asObservable();

  isAuthenticated(): boolean {
    if (typeof window === 'undefined') {
      return false; // SSR safe default
    }
    // Check memory-only token (single source of truth)
    return !!this.currentToken;
  }

  private decodeJwt(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Failed to decode JWT', e);
      return null;
    }
  }

  // Extract user data from current token on-demand
  private getTokenPayload(): any {
    if (!this.currentToken) return null;
    return this.decodeJwt(this.currentToken);
  }

  getEmail(): string | null {
    if (typeof window === 'undefined') return null;
    const payload = this.getTokenPayload();
    return payload?.sub || null;
  }

  getUsername(): string | null {
    if (typeof window === 'undefined') return null;
    const payload = this.getTokenPayload();
    return payload?.username || null;
  }

  getRoles(): string[] {
    if (typeof window === 'undefined') return [];
    const payload = this.getTokenPayload();
    if (!payload?.roles || !Array.isArray(payload.roles)) return [];
    // Normalize roles to remove any "ROLE_" prefix
    return payload.roles.map((r: string) => 
      r && r.startsWith('ROLE_') ? r.substring(5) : r
    );
  }

  clear(): void {
    if (typeof window === 'undefined') return;
    // Clear memory-only token
    this.currentToken = null;
    // Notify subscribers that auth state has changed (logged out)
    this.authStateChanged.next(false);
  }

  isAdmin(): boolean {
    const roles = this.getRoles();
    return roles.includes('ADMIN');
  }
  
  // Get current token from memory (single source of truth)
  getToken(): string | null {
    return this.currentToken;
  }
  
  // Set token in memory only (called by TokenRefreshInterceptor and login)
  setToken(token: string | null): void {
    this.currentToken = token;
    // Notify auth state changed when token is set
    if (token) {
      this.authStateChanged.next(true);
    }
  }
}

