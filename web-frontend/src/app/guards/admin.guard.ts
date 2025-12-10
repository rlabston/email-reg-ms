import { inject } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { CanActivateFn, CanMatchFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const auth = inject(AuthService);

  if (!isPlatformBrowser(platformId)) {
    // SSR: allow natural rendering
    return true;
  }

  const isAuth = auth.isAuthenticated();
  const isAdminUser = auth.isAdmin();
  
  console.log('[adminGuard] isAuthenticated:', isAuth, 'isAdmin:', isAdminUser);
  console.log('[adminGuard] roles:', auth.getRoles());

  // Check if authenticated AND has admin role
  if (!isAuth) {
    console.log('[adminGuard] NOT authenticated - redirecting to /login');
    return router.parseUrl('/login') as UrlTree;
  }

  if (!isAdminUser) {
    // Authenticated but not admin - redirect to home
    console.log('[adminGuard] authenticated but NOT admin - redirecting to /home');
    return router.parseUrl('/home') as UrlTree;
  }

  console.log('[adminGuard] ALLOWED - user is authenticated admin');
  return true;
};

export const adminMatchGuard: CanMatchFn = () => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const auth = inject(AuthService);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login') as UrlTree;
  }

  if (!auth.isAdmin()) {
    return router.parseUrl('/home') as UrlTree;
  }

  return true;
};
