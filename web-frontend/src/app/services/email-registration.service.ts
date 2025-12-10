import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { EmailRegistrationRequest, EmailRegistrationResponse } from '../models/email-registration.model';
import { RegisteredEmailDto } from '../models/registered-email.model';
import { LoginRequest, LoginResponse } from '../models/login.model';

@Injectable({
  providedIn: 'root'
})
export class EmailRegistrationService {
  private apiUrl = '/api/emails/register';
  private listUrl = '/api/emails';
  private loginUrl = '/api/emails/login';
  private logoutUrl = '/api/auth/logout';
  private deleteByEmailUrl = '/api/emails/email';
  private updateUserUrl = '/api/emails';

  // If the frontend is served from the dev server (localhost:4200) and
  // the proxy isn't configured/used, fall back to the backend host so
  // XHRs don't hit the static server which returns 501 for POSTs.
  private apiBase: string = (() => {
    try {
  if (typeof window !== 'undefined' && window.location.hostname === 'localhost' && window.location.port === '4200') {
  // Use the gateway as the public entrypoint (127.0.0.1:8080)
  return 'http://127.0.0.1:8080';
      }
    } catch (e) {
      // ignore
    }
    return '';
  })();

  private getApi(path: string) { return this.apiBase + path; }

  constructor(private http: HttpClient) { }

  registerEmail(request: EmailRegistrationRequest): Observable<EmailRegistrationResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<EmailRegistrationResponse>(this.getApi(this.apiUrl), request, { headers });
  }

  private authHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    const token = (typeof window !== 'undefined') ? localStorage.getItem('auth_token') : null;
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }
    return headers;
  }

  getAllRegisteredEmails(): Observable<RegisteredEmailDto[]> {
    const headers = this.authHeaders();
    return this.http.get<RegisteredEmailDto[]>(this.getApi(this.listUrl), { headers });
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    try { console.log('[email-service] POST', this.loginUrl, request); } catch(e){}
    return this.http.post<LoginResponse>(this.getApi(this.loginUrl), request, { headers });
  }

  logout(): Observable<any> {
    const headers = this.authHeaders();
    return this.http.post<any>(this.getApi(this.logoutUrl), {}, { headers });
  }

  deleteByEmail(email: string): Observable<any> {
    // DELETE /api/emails/email?email=...
    const headers = this.authHeaders();
    return this.http.delete<any>(this.getApi(this.deleteByEmailUrl), { params: { email } as any, headers });
  }

  getCurrentUser(): Observable<any> {
    const headers = this.authHeaders();
    return this.http.get<any>(this.getApi('/api/auth/me'), { headers });
  }

  updateUser(request: any): Observable<any> {
    const headers = this.authHeaders();
    return this.http.put<any>(this.getApi(`${this.updateUserUrl}/${request.id}`), request, { headers });
  }

  getUserWithRoles(id: number): Observable<any> {
    const headers = this.authHeaders();
    return this.http.get<any>(this.getApi(`/api/emails/with-roles/${id}`), { headers });
  }

  sendChatMessage(message: string, conversationId: string | null): Promise<any> {
    const headers = this.authHeaders();
    return firstValueFrom(
      this.http.post<any>(this.getApi('/api/chat/message'), 
        { message, conversationId },
        { headers }
      )
    );
  }
}
