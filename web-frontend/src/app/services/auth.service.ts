import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private emailKey = 'auth_email';
  private usernameKey = 'auth_username';
  private rolesKey = 'auth_roles';
  private tokenKey = 'auth_token';
  private tokenExpiresAtKey = 'auth_token_expires_at';

  isAuthenticated(): boolean {
    if (typeof window === 'undefined') {
      return false; // SSR safe default
    }
    // Consider the client authenticated only if we have both an email and roles cached.
    // This prevents stale/local-only email keys from making the app redirect away from the
    // login page before we can refresh roles from the server.
    return !!localStorage.getItem(this.emailKey) && !!localStorage.getItem(this.rolesKey);
  }

  getEmail(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.emailKey);
  }

  getUsername(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(this.usernameKey);
  }

  setSession(email: string, username?: string, roles?: string[]): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(this.emailKey, email);
    if (username) {
      localStorage.setItem(this.usernameKey, username);
    }
    if (roles) {
      try {
        // Normalize roles to remove any "ROLE_" prefix so the client
        // can reliably check for values like 'ADMIN'. Backend responses
        // may contain roles with or without the ROLE_ prefix depending
        // on which endpoint was used (login vs /auth/me).
        const normalized = roles.map(r => r && r.startsWith('ROLE_') ? r.substring(5) : r);
        localStorage.setItem(this.rolesKey, JSON.stringify(normalized));
      } catch (e) {
        // ignore storage errors
      }
    }
  }

  getRoles(): string[] {
    if (typeof window === 'undefined') return [];
    const raw = localStorage.getItem(this.rolesKey);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as string[];
    } catch (e) {
      return [];
    }
  }

  clear(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(this.emailKey);
    localStorage.removeItem(this.usernameKey);
    localStorage.removeItem(this.rolesKey);
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.tokenExpiresAtKey);
  }

  isAdmin(): boolean {
    const roles = this.getRoles();
    return roles.includes('ADMIN');
  }
}

