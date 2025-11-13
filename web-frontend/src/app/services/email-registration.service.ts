import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmailRegistrationRequest, EmailRegistrationResponse } from '../models/email-registration.model';

@Injectable({
  providedIn: 'root'
})
export class EmailRegistrationService {
  private apiUrl = 'http://localhost:8080/api/emails/register';

  constructor(private http: HttpClient) { }

  registerEmail(request: EmailRegistrationRequest): Observable<EmailRegistrationResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<EmailRegistrationResponse>(this.apiUrl, request, { headers });
  }
}
