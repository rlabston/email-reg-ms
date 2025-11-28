import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { RegistrationComponent } from './registration/registration.component';
import { EmailListComponent } from './email-list/email-list.component';
import { authGuard, authMatchGuard } from './guards/auth.guard';
import { ChatbotComponent } from './chatbot/chatbot.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: HomeComponent
  },
  {
    path: 'register',
    component: RegistrationComponent
  },
  {
    path: 'emails',
    component: EmailListComponent,
    canMatch: [authMatchGuard],
    canActivate: [authGuard]
  },
  {
    path: 'chatbot',
    component: ChatbotComponent,
    canMatch: [authMatchGuard],
    canActivate: [authGuard]
  }
];
