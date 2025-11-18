import { inject } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { CanActivateFn, CanMatchFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const auth = inject(AuthService);

  if (!isPlatformBrowser(platformId)) {
    // SSR: if not authenticated, allow SSR to render whatever route was requested
    // (login page will render, protected routes will redirect on client hydration)
    return !auth.isAuthenticated() ? true : true;
  }

  return auth.isAuthenticated() ? true : router.parseUrl('/login') as UrlTree;
};

// Also expose a canMatch variant to ensure redirects occur during initial route matching (helps with SSR/hydration)
export const authMatchGuard: CanMatchFn = () => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const auth = inject(AuthService);

  if (!isPlatformBrowser(platformId)) {
    // SSR: allow natural route rendering
    return true;
  }

  return auth.isAuthenticated() ? true : (router.parseUrl('/login') as UrlTree);
};
