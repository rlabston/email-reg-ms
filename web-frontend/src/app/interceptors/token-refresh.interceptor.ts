import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor that reads X-New-JWT from responses and updates the in-memory token.
 * Backend is the single source of truth - token is stored in memory only, not localStorage.
 */
@Injectable()
export class TokenRefreshInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap((event) => {
        if (event instanceof HttpResponse) {
          const newJwt = event.headers.get('X-New-JWT');
          if (newJwt) {
            // Store token in memory only - backend is single source of truth
            this.auth.setToken(newJwt);
          }
        }
      })
    );
  }
}
