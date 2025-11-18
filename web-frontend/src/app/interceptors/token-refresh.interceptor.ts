import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor that reads X-New-JWT and X-JWT-Expires-In from responses and
 * persists refreshed tokens to localStorage (via AuthService).
 */
@Injectable()
export class TokenRefreshInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap((event) => {
        if (event instanceof HttpResponse) {
          const newJwt = event.headers.get('X-New-JWT');
          const expiresMs = event.headers.get('X-JWT-Expires-In');
          if (newJwt) {
            try {
              // persist token and computed expiry fields the same way login does
              localStorage.setItem('auth_token', newJwt);
              if (expiresMs) {
                const exp = Number(expiresMs);
                localStorage.setItem('auth_token_expires_in_ms', String(exp));
                localStorage.setItem('auth_token_expires_at', String(Date.now() + exp));
              }
            } catch (e) {
              // ignore storage errors
            }
          }
        }
      })
    );
  }
}
