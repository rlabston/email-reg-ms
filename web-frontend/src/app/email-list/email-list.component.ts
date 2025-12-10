import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EmailRegistrationService } from '../services/email-registration.service';
import { RegisteredEmailDto } from '../models/registered-email.model';

@Component({
  selector: 'app-email-list',
  imports: [CommonModule],
  template: `
    <div class="list-container">
      <div class="list-card">
        <div class="list-header">
          <h2>Registered Emails</h2>
          <button type="button" (click)="loadEmails()" [disabled]="listLoading()">
            <span *ngIf="!listLoading()">Refresh</span>
            <span *ngIf="listLoading()">Loading...</span>
          </button>
        </div>

        <div *ngIf="emails().length === 0 && !listLoading()" class="empty-state">
          No emails loaded yet. Click "Refresh" to load the list.
        </div>

        <table *ngIf="emails().length > 0" class="emails-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>Username</th>
              <th>Registered</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of emails()" (click)="selectEmail(e)" [class.selected]="selectedEmail() === e.email">
              <td>{{ e.id }}</td>
              <td>{{ e.email }}</td>
              <td>{{ e.username }}</td>
              <td>{{ e.registrationDate }}</td>
            </tr>
          </tbody>
        </table>

        <div class="list-actions" style="margin-top:12px;" *ngIf="emails().length > 0">
          <button type="button" (click)="editSelected()" [disabled]="!selectedEmail()">Edit Selected</button>
          <button type="button" (click)="deleteSelected()" [disabled]="!selectedEmail()">Delete Selected</button>
        </div>

        <div 
          *ngIf="statusMessage()" 
          class="status-message"
          [ngClass]="{ 'error': isError() }"
        >
          {{ statusMessage() }}
        </div>
      </div>
    </div>
  `,
  styleUrl: '../app.css'
})
export class EmailListComponent implements OnInit {
  emails = signal<RegisteredEmailDto[]>([]);
  selectedEmail = signal<string | null>(null);
  selectedEmailData = signal<RegisteredEmailDto | null>(null);
  listLoading = signal(false);
  statusMessage = signal('');
  isError = signal(false);

  constructor(
    private emailService: EmailRegistrationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Load emails when component initializes
    this.loadEmails();
  }

  selectEmail(e: RegisteredEmailDto): void {
    if (this.selectedEmail() === e.email) {
      this.selectedEmail.set(null);
      this.selectedEmailData.set(null);
    } else {
      this.selectedEmail.set(e.email);
      this.selectedEmailData.set(e);
    }
  }

  editSelected(): void {
    const emailData = this.selectedEmailData();
    if (!emailData) return;
    
    // Fetch full user data with roles before navigating
    this.emailService.getUserWithRoles(emailData.id).subscribe({
      next: (userData) => {
        this.router.navigate(['/register'], {
          queryParams: {
            edit: 'true',
            id: userData.id,
            email: userData.email,
            username: userData.username,
            roles: userData.roles?.join(',') || ''
          }
        });
      },
      error: (err) => {
        console.error('Failed to fetch user roles', err);
        // Fallback: navigate without roles
        this.router.navigate(['/register'], {
          queryParams: {
            edit: 'true',
            id: emailData.id,
            email: emailData.email,
            username: emailData.username,
            roles: ''
          }
        });
      }
    });
  }

  deleteSelected(): void {
    const email = this.selectedEmail();
    if (!email) return;
    
    this.emailService.deleteByEmail(email).subscribe({
      next: () => {
        this.selectedEmail.set(null);
        this.showSuccess('Email deleted successfully');
        this.loadEmails();
      },
      error: (err) => {
        console.error('Delete failed', err);
        this.showError('Delete failed: ' + (err.message || 'Unknown'));
      }
    });
  }

  loadEmails(): void {
    this.listLoading.set(true);
    this.statusMessage.set('');
    
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
}
